package io.github.hossensyedriadh.atm_terminal.cli;

import io.github.hossensyedriadh.atm_terminal.configuration.properties.AtmTerminalSessionProperties;
import org.jdom2.output.LineSeparator;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public final class ConsoleInput implements AutoCloseable {
    private final BufferedReader reader;
    private final PrintWriter writer;
    private boolean previousWasCR;
    private final Socket socket;
    private final AtmTerminalSessionProperties sessionProperties;
    private final long sessionStartNanos;

    public ConsoleInput(Socket socket, AtmTerminalSessionProperties sessionProperties) throws IOException {
        this.socket = socket;
        this.sessionProperties = sessionProperties;
        this.sessionStartNanos = System.nanoTime();

        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
    }

    public String readPan() throws IOException {
        return this.readDigits("Enter PAN: ", 12, 19, false);
    }

    public String readPin() throws IOException {
        return this.readDigits("Enter PIN: ", 4, 12, true);
    }

    public String readAmount(String prompt) throws IOException {
        return this.readDigits(prompt, 3, 12, false);
    }

    public String readLine(String prompt) throws IOException {
        this.writer.print(prompt);
        this.writer.flush();

        return this.readInput(false);
    }

    private String readDigits(String prompt, int minLength, int maxLength, boolean masked) throws IOException {
        while (true) {
            this.writer.print(prompt);
            this.writer.flush();

            String value = this.readInput(masked);

            if (value.matches("[0-9]{" + minLength + "," + maxLength + "}")) {
                return value;
            }

            this.writer.printf("Enter between %d and %d digits.\r\n", minLength, maxLength);
            this.writer.flush();
        }
    }

    private String readInput(boolean masked) throws IOException {
        StringBuilder value = new StringBuilder();

        while (true) {
            this.configureReadTimeout();

            int input;

            try {
                input = this.reader.read();
            } catch (SocketTimeoutException e) {
                this.handleTimeout();
                throw e;
            }

            if (input == -1) {
                throw new EOFException("ATM terminal client disconnected");
            }

            char character = (char) input;

            if (character == '\r') {
                this.previousWasCR = true;
                this.newLine();
                this.writer.flush();
                return value.toString();
            }

            if (character == '\n') {
                if (this.previousWasCR) {
                    this.previousWasCR = false;
                    continue;
                }

                this.newLine();
                this.writer.flush();
                return value.toString();
            }

            this.previousWasCR = false;

            if (character == '\b' || character == 127) {
                if (!value.isEmpty()) {
                    value.deleteCharAt(value.length() - 1);
                    this.writer.print("\b \b");
                    this.writer.flush();
                }
                continue;
            }

            if (character >= '0' && character <= '9') {
                value.append(character);
                this.writer.print(masked ? '*' : character);
                this.writer.flush();
            }
        }
    }

    private void configureReadTimeout() throws IOException {
        long elapsed = System.nanoTime() - this.sessionStartNanos;
        long maxDurationNanos = this.sessionProperties.maxDuration().toNanos();

        long remainingNanos = maxDurationNanos - elapsed;

        if (remainingNanos <= 0) {
            this.handleTimeout();
        }

        long idleTimeoutMillis = this.sessionProperties.idleTimeout().toMillis();

        long remainingMillis = Math.max(
                1,
                TimeUnit.NANOSECONDS.toMillis(remainingNanos - 1) + 1
        );

        long timeoutMillis = Math.min(idleTimeoutMillis, remainingMillis);

        int socketTimeoutMillis = Math.clamp(timeoutMillis, 1, Integer.MAX_VALUE);

        this.socket.setSoTimeout(socketTimeoutMillis);
    }

    private void handleTimeout() throws IOException {
        long elapsed = System.nanoTime() - this.sessionStartNanos;

        if (elapsed >= this.sessionProperties.maxDuration().toNanos()) {
            this.writer.print(LineSeparator.CRNL.value() + "Maximum ATM session duration exceeded." + LineSeparator.CRNL.value());
        } else {
            this.writer.print(LineSeparator.CRNL.value() + "ATM session terminated due to inactivity." + LineSeparator.CRNL.value());
        }

        this.writer.flush();

        throw new SessionTimeoutException("ATM session timeout");
    }

    public void clearScreen() {
        this.writer.print("\033[2J\033[H");
        this.writer.flush();
    }

    public PrintWriter writer() {
        return this.writer;
    }

    @Override
    public void close() {
        this.writer.flush();
    }

    private void newLine() {
        this.writer.print(LineSeparator.CRNL.value());
        this.writer.flush();
    }

    public static class SessionTimeoutException extends IOException {
        @Serial
        private static final long serialVersionUID = 3075786328113247549L;

        public SessionTimeoutException(String message) {
            super(message);
        }
    }
}
