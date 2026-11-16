package io.github.hossensyedriadh.atm_terminal.cli;

import org.jdom2.output.LineSeparator;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class ConsoleInput implements AutoCloseable {
    private final BufferedReader reader;
    private final PrintWriter writer;
    private boolean previousWasCR;

    public ConsoleInput(InputStream input, OutputStream output) {
        this.reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        this.writer = new PrintWriter(output, true, StandardCharsets.UTF_8);
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
            int input = this.reader.read();

            if (input == -1) {
                throw new IOException("Client disconnected");
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
}
