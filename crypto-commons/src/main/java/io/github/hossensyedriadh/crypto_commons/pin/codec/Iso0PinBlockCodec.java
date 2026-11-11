package io.github.hossensyedriadh.crypto_commons.pin.codec;

import java.util.HexFormat;

public final class Iso0PinBlockCodec {
    private static final int BLOCK_HEX_LENGTH = 16;

    private Iso0PinBlockCodec() {
    }

    public static byte[] encode(String pan, String pin) {
        validatePan(pan);
        validatePin(pin);

        String pinField = "0" + pin.length() + pin + "F".repeat(BLOCK_HEX_LENGTH - 2 - pin.length());

        String panField = "0000" + pan.substring(pan.length() - 13, pan.length() - 1);

        String blockHex = xorHex(pinField, panField);
        return HexFormat.of().parseHex(blockHex);
    }

    public static String decode(String pan, byte[] pinBlock) {
        validatePan(pan);

        if (pinBlock == null || pinBlock.length != 8) {
            throw new IllegalArgumentException("ISO-0 PIN block must be exactly 8 bytes");
        }

        String encryptedHex = HexFormat.of().withUpperCase().formatHex(pinBlock);

        String panField = "0000" + pan.substring(pan.length() - 13, pan.length() - 1);

        String pinField = xorHex(encryptedHex, panField);

        if (pinField.charAt(0) != '0') {
            throw new IllegalArgumentException("Invalid ISO-0 format nibble");
        }

        int pinLength = Character.digit(pinField.charAt(1), 10);

        if (pinLength < 4 || pinLength > 12) {
            throw new IllegalArgumentException("Invalid PIN length");
        }

        String pin = pinField.substring(2, 2 + pinLength);
        String padding = pinField.substring(2 + pinLength);

        if (!pin.matches("\\d{" + pinLength + "}") || !padding.matches("F*")) {
            throw new IllegalArgumentException("Invalid ISO-0 PIN field");
        }

        return pin;
    }

    private static String xorHex(String first, String second) {
        if (first.length() != 16 || second.length() != 16) {
            throw new IllegalArgumentException("PIN block fields must be 16 hexadecimal characters");
        }

        StringBuilder result = new StringBuilder(16);

        for (int i = 0; i < 16; i++) {
            int a = Character.digit(first.charAt(i), 16);
            int b = Character.digit(second.charAt(i), 16);

            if (a < 0 || b < 0) {
                throw new IllegalArgumentException("Invalid hexadecimal data");
            }

            result.append(Character.toUpperCase(Character.forDigit(a ^ b, 16)));
        }

        return result.toString();
    }

    private static void validatePan(String pan) {
        if (pan == null || !pan.matches("\\d{13,19}")) {
            throw new IllegalArgumentException("ISO-0 requires a 13-to-19-digit PAN");
        }
    }

    private static void validatePin(String pin) {
        if (pin == null || !pin.matches("\\d{4,12}")) {
            throw new IllegalArgumentException("PIN must contain 4 to 12 digits");
        }
    }
}
