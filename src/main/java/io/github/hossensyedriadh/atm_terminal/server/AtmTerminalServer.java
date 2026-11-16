package io.github.hossensyedriadh.atm_terminal.server;

import io.github.hossensyedriadh.atm_terminal.session.AtmSessionManager;
import io.github.hossensyedriadh.atm_terminal.session.AtmTerminalSession;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
@Component
public class AtmTerminalServer implements SmartLifecycle {
    private final AtmSessionManager atmSessionManager;
    private final AtmTerminalSession atmTerminalSession;
    private final HealthCheckServer healthCheckServer;

    private final int port;

    private final AtomicReference<Socket> activeSocket = new AtomicReference<>();
    private volatile boolean running;

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private Thread serverThread;

    public AtmTerminalServer(AtmSessionManager atmSessionManager, AtmTerminalSession atmTerminalSession,
                             HealthCheckServer healthCheckServer, @Value("${atm.tcp-server.port}") int port) {
        this.atmSessionManager = atmSessionManager;
        this.atmTerminalSession = atmTerminalSession;
        this.healthCheckServer = healthCheckServer;
        this.port = port;
    }

    @Override
    public synchronized void start() {
        if (this.running) {
            return;
        }

        try {
            this.serverSocket = new ServerSocket();
            this.serverSocket.setReuseAddress(true);
            this.serverSocket.bind(new InetSocketAddress("0.0.0.0", this.port));

            this.executorService = Executors.newVirtualThreadPerTaskExecutor();
            this.running = true;

            this.serverThread = Thread.ofVirtual().name("atm-terminal-server")
                    .start(this::acceptConnections);

            this.healthCheckServer.setReady(true);

            log.info("ATM terminal TCP server listening on port {}", this.port);
        } catch (IOException e) {
            this.running = false;

            if (this.serverSocket != null) {
                try {
                    this.serverSocket.close();
                } catch (IOException closeException) {
                    e.addSuppressed(closeException);
                }
            }

            if (this.executorService != null) {
                this.executorService.shutdownNow();
            }

            throw new IllegalStateException("Could not start ATM terminal TCP server on port: " + this.port, e);
        }
    }

    private void acceptConnections() {
        while (this.running) {
            try {
                Socket socket = this.serverSocket.accept();

                AtmSessionManager.SessionLease lease = this.atmSessionManager.tryAcquire();

                if (lease == null || this.atmSessionManager.isOccupied()) {
                    this.rejectConnection(socket);
                    continue;
                }

                this.activeSocket.set(socket);

                try {
                    this.executorService.submit(() -> this.handleSession(socket, lease));
                } catch (RuntimeException re) {
                    this.activeSocket.compareAndSet(socket, null);
                    lease.close();
                    this.closeSocket(socket);

                    log.error("Could not start ATM terminal session", re);
                }
            } catch (IOException e) {
                if (this.running) {
                    log.error("Error accepting ATM terminal connection", e);
                }
            }
        }
    }

    private void handleSession(Socket socket, AtmSessionManager.SessionLease lease) {
        try (lease; socket) {
            log.info("ATM terminal client connected: {}", socket.getRemoteSocketAddress());

            this.atmTerminalSession.run(socket);
        } catch (Exception e) {
            log.error("ATM Terminal session failed", e);
        } finally {
            this.activeSocket.compareAndSet(socket, null);

            log.info("ATM terminal is now available");
        }
    }

    private void rejectConnection(Socket socket) {
        try (socket; PrintWriter output = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
            output.println();
            output.println("================================");
            output.println("       Welcome to Dummy ATM");
            output.println("================================");
            output.println();
            output.println("ATM is currently in use.");
            output.println("Please try again later.");
            output.println();
            output.println("Connection closing...");

            log.info("Rejected connection because ATM is occupied: {}", socket.getRemoteSocketAddress());
        } catch (IOException e) {
            log.debug("Could not send ATM busy message: {}", e.getMessage());
        }
    }

    private void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            log.debug("Could not close ATM socket", e);
        }
    }

    @Override
    public synchronized void stop() {
        if (!this.running) {
            return;
        }

        this.running = false;

        try {
            this.serverSocket.close();
        } catch (IOException e) {
            log.warn("Could not close ATM server socket", e);
        }

        Socket socket = this.activeSocket.getAndSet(null);

        if (socket != null) {
            this.closeSocket(socket);
        }

        if (this.executorService != null) {
            this.executorService.shutdownNow();
        }

        if (this.serverThread != null) {
            this.serverThread.interrupt();
        }

        this.healthCheckServer.setReady(false);

        log.info("ATM terminal TCP server stopped.");
    }

    @Override
    public void stop(@NonNull Runnable callback) {
        this.stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
