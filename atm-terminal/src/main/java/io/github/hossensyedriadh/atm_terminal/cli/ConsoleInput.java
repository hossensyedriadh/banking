package io.github.hossensyedriadh.atm_terminal.cli;

import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Widget;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

public final class ConsoleInput implements AutoCloseable {
    private final Terminal terminal;
    private final LineReader reader;

    public ConsoleInput() throws IOException {
        this.terminal = TerminalBuilder.builder().system(true).build();
        this.reader = LineReaderBuilder.builder().terminal(terminal).build();

        this.bindDigitOnlyInput();
    }

    private void bindDigitOnlyInput() {
        KeyMap<Binding> keyMap = this.reader.getKeyMaps().get(LineReader.MAIN);

        for (char c = 32; c <= 126; c++) {
            final char character = c;

            Widget widget = () -> {
                if (character >= '0' && character <= '9' && reader.getBuffer().length() < 19) {
                    this.reader.getBuffer().write(character);
                }

                return true;
            };

            this.reader.getWidgets().put("digit-" + (int) character, widget);

            keyMap.bind(widget, String.valueOf(character));
        }
    }

    public String readPan() {
        return this.readDigits("Enter PAN: ", 12, 19, null);
    }

    public String readPin() {
        this.reader.setVariable(LineReader.DISABLE_HISTORY, true);

        try {
            return this.readDigits("Enter PIN: ", 4, 12, '*');
        } finally {
            this.reader.setVariable(LineReader.DISABLE_HISTORY, false);
        }
    }

    public String readLine(String prompt) {
        return this.reader.readLine(prompt);
    }

    public String readAmount(String prompt) {
        return this.readDigits(prompt, 3, 12, null);
    }

    private String readDigits(String prompt, int minLength, int maxLength, Character mask) {
        while (true) {
            String value = this.reader.readLine(prompt, mask);

            if (value.matches("[0-9]{" + minLength + "," + maxLength + "}")) {
                return value;
            }

            this.terminal.writer().printf("Enter between %d and %d digits.%n", minLength, maxLength);
            this.terminal.flush();
        }
    }

    public void clearScreen() {
        this.terminal.puts(org.jline.utils.InfoCmp.Capability.clear_screen);
        this.terminal.flush();
    }

    @Override
    public void close() throws IOException {
        this.terminal.close();
    }
}
