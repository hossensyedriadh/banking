package io.github.hossensyedriadh.cbs.card.api.command;

public record PinBlockVerificationCommand(String pan, byte[] encryptedPinBlock, String pinVerificationData,
                                          int pinLength) {
    public PinBlockVerificationCommand {
        if (pan == null || pan.isBlank()) {
            throw new IllegalArgumentException("PAN is required");
        }

        if (encryptedPinBlock == null || encryptedPinBlock.length != 8) {
            throw new IllegalArgumentException("Encrypted PIN block must be exactly 8 bytes");
        }

        encryptedPinBlock = encryptedPinBlock.clone();

        if (pinLength != 4) {
            throw new IllegalArgumentException("Simulator currently supports only four-digit PINs");
        }
    }

    public byte[] encryptedPinBlock() {
        return encryptedPinBlock.clone();
    }
}
