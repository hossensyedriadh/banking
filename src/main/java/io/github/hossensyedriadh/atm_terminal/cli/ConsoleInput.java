package io.github.hossensyedriadh.atm_terminal.cli;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class ConsoleInput implements AutoCloseable {
    private final BufferedReader reader;
    private final PrintWriter writer;
    private boolean previousWasCR;

    public ConsoleInput(InputStream input, OutputStream output) {
        this.reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8)
        );

        this.writer = new PrintWriter(output, true, StandardCharsets.UTF_8);
    }

    public String readPan() throws IOException {
        return readDigits("Enter PAN: ", 12, 19, false);
    }

    public String readPin() throws IOException {
        return readDigits("Enter PIN: ", 4, 12, true);
    }

    public String readAmount(String prompt) throws IOException {
        return readDigits(prompt, 3, 12, false);
    }

    public String readLine(String prompt) throws IOException {
        writer.print(prompt);
        writer.flush();

        return readInput(false);
    }

    private String readDigits(
            String prompt,
            int minLength,
            int maxLength,
            boolean masked
    ) throws IOException {

        while (true) {
            writer.print(prompt);
            writer.flush();

            String value = readInput(masked);

            if (value.matches("[0-9]{" + minLength + "," + maxLength + "}")) {
                return value;
            }

            writer.printf(
                    "Enter between %d and %d digits.%n",
                    minLength,
                    maxLength
            );
            writer.flush();
        }
    }

    private String readInput(boolean masked) throws IOException {
        StringBuilder value = new StringBuilder();

        while (true) {
            int input = reader.read();

            if (input == -1) {
                throw new IOException("Client disconnected");
            }

            char character = (char) input;

            if (character == '\r') {
                previousWasCR = true;
                writer.println();
                writer.flush();
                return value.toString();
            }

            if (character == '\n') {
                if (previousWasCR) {
                    previousWasCR = false;
                    continue;
                }

                writer.println();
                writer.flush();
                return value.toString();
            }

            previousWasCR = false;

            if (character == '\b' || character == 127) {
                if (!value.isEmpty()) {
                    value.deleteCharAt(value.length() - 1);
                    writer.print("\b \b");
                    writer.flush();
                }
                continue;
            }

            if (character >= '0' && character <= '9') {
                value.append(character);
                writer.print(masked ? '*' : character);
                writer.flush();
            }
        }
    }

    public void clearScreen() {
        writer.print("\033[2J\033[H");
        writer.flush();
    }

    public PrintWriter writer() {
        return writer;
    }

    @Override
    public void close() throws IOException {
        writer.flush();
    }
}
