package io.github.hossensyedriadh.cbs.card.internal.util;

public final class CardPinUtil {
    public static boolean isValidCardPin(String pin) {
        if (pin == null || !pin.matches("\\d{4}")) {
            return false;
        }

        if (pin.chars().distinct().count() == 1) {
            return false;
        }

        boolean ascending = true;
        boolean descending = true;

        for (int i = 1; i < pin.length(); i++) {
            int prev = pin.charAt(i - 1) - '0';
            int curr = pin.charAt(i) - '0';

            if (curr != prev + 1) {
                ascending = false;
            }

            if (curr != prev - 1) {
                descending = false;
            }
        }

        if (ascending || descending) {
            return false;
        }

        for (int patternLength = 1; patternLength <= pin.length() / 2; patternLength++) {
            if (pin.length() % patternLength != 0) {
                continue;
            }

            String pattern = pin.substring(0, patternLength);
            String repeated = pattern.repeat(pin.length() / patternLength);

            if (repeated.equals(pin)) {
                return false;
            }
        }

        return true;
    }
}
