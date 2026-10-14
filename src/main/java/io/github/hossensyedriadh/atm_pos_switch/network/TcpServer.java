package io.github.hossensyedriadh.atm_pos_switch.network;

import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log4j2
@Component
public class TcpServer implements SmartLifecycle {
    private static final int BUFFER_SIZE = 4096;

    private final TcpServerProperties properties;

    private volatile boolean running;
    private ServerSocket serverSocket;
    private ExecutorService connectionExecutor;
    private Thread acceptThread;
    private final Set<Socket> activeSockets = ConcurrentHashMap.newKeySet();

    @Autowired
    public TcpServer(TcpServerProperties properties) {
        this.properties = properties;
    }

    @Override
    public synchronized void start() {
        if (running) {
            return;
        }

        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(properties.host(), properties.port()));

            connectionExecutor = Executors.newVirtualThreadPerTaskExecutor();
            running = true;

            acceptThread = Thread.ofVirtual().name("switch-tcp-accept").start(this::acceptLoop);

            log.info("ATM/POS TCP server started on {}:{}", properties.host(), properties.port());
        } catch (IOException ioe) {
            closeServerSocket();

            log.error("Failed to start ATM/POS TCP listener on {}:{}", properties.host(), properties.port(), ioe);

            throw new RuntimeException("Failed to start ATM/POS TCP listener on " + properties.host() + ":" + properties.port(), ioe);
        }
    }

    @Override
    public synchronized void stop() {
        if (!running) {
            closeServerSocket();
            return;
        }

        running = false;

        closeServerSocket();

        activeSockets.forEach(this::closeSocket);

        if (connectionExecutor != null) {
            connectionExecutor.shutdownNow();
        }

        if (acceptThread != null) {
            acceptThread.interrupt();
        }

        log.info("ATM/POS TCP server stopped");
    }

    @Override
    public void stop(@NonNull Runnable callback) {
        try {
            stop();
        } finally {
            callback.run();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                socket.setTcpNoDelay(true);
                socket.setKeepAlive(true);
                socket.setSoTimeout(properties.readTimeout());

                activeSockets.add(socket);

                log.info("Terminal TCP connection accepted from {}", socket.getRemoteSocketAddress());

                try {
                    connectionExecutor.submit(() -> handleConnection(socket));
                } catch (RuntimeException exception) {
                    activeSockets.remove(socket);
                    closeSocket(socket);
                    throw exception;
                }
            } catch (SocketException exception) {
                if (running) {
                    log.error("TCP accept failed", exception);
                }
                break;
            } catch (IOException exception) {
                if (running) {
                    log.error("Failed to accept terminal connection", exception);
                }
            }
        }
    }

    private void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException exception) {
            log.debug("Error closing socket: {}", exception.getMessage());
        }
    }

    private void handleConnection(Socket socket) {
        long lastActivity = System.nanoTime();

        try (socket; InputStream inputStream = socket.getInputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];

            while (running && !socket.isClosed()) {
                try {
                    int bytesRead = inputStream.read(buffer);

                    if (bytesRead == -1) {
                        log.info("Terminal disconnected: {}", socket.getRemoteSocketAddress());
                        break;
                    }

                    lastActivity = System.nanoTime();

                    log.debug("Received {} TCP bytes from {}", bytesRead, socket.getRemoteSocketAddress());

                    //todo: frame ISO8583
                } catch (SocketTimeoutException exception) {
                    long idleMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastActivity);

                    if (idleMillis >= properties.idleTimeout()) {
                        log.info("Closing idle terminal connection: {}", socket.getRemoteSocketAddress());
                        break;
                    }
                }
            }
        } catch (IOException exception) {
            if (running) {
                log.debug("Terminal connection ended: {}", socket.getRemoteSocketAddress());
            }
        } finally {
            activeSockets.remove(socket);
            log.debug("Terminal connection handler finished");
        }
    }

    private void closeServerSocket() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                log.debug("TCP server socket closed");
            } catch (IOException ioe) {
                log.error("Failed to close TCP server socket", ioe);
            }
        }
    }
}
