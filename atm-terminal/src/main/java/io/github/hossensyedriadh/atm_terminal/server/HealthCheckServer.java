
package io.github.hossensyedriadh.atm_terminal.server;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
@Component
public class HealthCheckServer implements SmartLifecycle {
    private final int port;

    private final AtomicBoolean ready = new AtomicBoolean(false);

    private volatile boolean running;
    private ServerSocket serverSocket;
    private Thread serverThread;

    public HealthCheckServer(@Value("${atm.health-check-server.port}") int port) {
        this.port = port;
    }

    @Override
    public synchronized void start() {
        if (this.running) {
            return;
        }

        try {
            this.serverSocket = new ServerSocket(this.port);
            this.running = true;

            this.serverThread = Thread.ofVirtual().name("health-check-server")
                    .start(this::acceptConnections);

            log.info("Health check server started on port {}", port);
        } catch (IOException exception) {
            this.running = false;

            throw new IllegalStateException("Failed to start health check server on port " + port, exception);
        }
    }

    private void acceptConnections() {
        while (this.running) {
            try {
                Socket socket = this.serverSocket.accept();
                this.handleRequest(socket);
            } catch (IOException exception) {
                if (this.running) {
                    log.error("Health check server encountered an error", exception);
                }
            }
        }
    }

    private void handleRequest(Socket socket) {
        try (socket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
             OutputStream output = socket.getOutputStream()) {

            socket.setSoTimeout(5000);

            String requestLine = reader.readLine();

            if (requestLine == null) {
                return;
            }

            String header;
            while ((header = reader.readLine()) != null && !header.isEmpty()) {
                // do nothing
            }

            String[] parts = requestLine.split(" ");

            if (parts.length < 2 || !parts[0].equals("GET")) {
                this.writeResponse(output, 400, "Bad Request");
                return;
            }

            switch (parts[1]) {
                case "/health/liveness" -> this.writeResponse(output, 200, "UP");
                case "/health/readiness" -> {
                    boolean isReady = this.ready.get();
                    this.writeResponse(
                            output,
                            isReady ? 200 : 503,
                            isReady ? "READY" : "NOT_READY"
                    );
                }

                default -> this.writeResponse(output, 404, "Not Found");
            }

        } catch (IOException exception) {
            log.debug("Health check connection failed: {}", exception.getMessage());
        }
    }

    private void writeResponse(OutputStream output, int status, String body) throws IOException {
        String reason = switch (status) {
            case 200 -> "OK";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 503 -> "Service Unavailable";
            default -> "Internal Server Error";
        };

        byte[] bodyBytes = body.getBytes(StandardCharsets.US_ASCII);

        String responseHeaders = """
                HTTP/1.1 %d %s\r
                Content-Type: text/plain; charset=utf-8\r
                Content-Length: %d\r
                Connection: close\r
                \r
                """.formatted(status, reason, bodyBytes.length);

        output.write(responseHeaders.getBytes(StandardCharsets.US_ASCII));
        output.write(bodyBytes);
        output.flush();
    }

    public void setReady(boolean ready) {
        this.ready.set(ready);
    }

    public boolean isReady() {
        return this.ready.get();
    }

    @Override
    public void stop() {
        this.running = false;
        this.ready.set(false);

        try {
            if (this.serverSocket != null) {
                this.serverSocket.close();
            }
        } catch (IOException exception) {
            log.warn("Error closing health check server socket", exception);
        }

        if (this.serverThread != null) {
            this.serverThread.interrupt();
        }

        log.info("Health check server stopped");
    }

    @Override
    public void stop(Runnable callback) {
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
