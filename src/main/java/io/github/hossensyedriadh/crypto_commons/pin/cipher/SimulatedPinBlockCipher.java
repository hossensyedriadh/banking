package io.github.hossensyedriadh.crypto_commons.pin.cipher;

import io.github.hossensyedriadh.crypto_commons.pin.exception.PinVerificationInfrastructureException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Base64;

public class SimulatedPinBlockCipher {
    private final SecretKeySpec key;

    public SimulatedPinBlockCipher(String encodedKey) {
        byte[] keyBytes = Base64.getDecoder().decode(encodedKey);

        if (keyBytes.length != 24) {
            throw new IllegalArgumentException("Simulator Triple DES key must be 24 bytes");
        }

        this.key = new SecretKeySpec(keyBytes, "DESede");
    }

    public byte[] encrypt(byte[] pinBlock) {
        return apply(pinBlock, Cipher.ENCRYPT_MODE);
    }

    public byte[] decrypt(byte[] encryptedPinBlock) {
        return apply(encryptedPinBlock, Cipher.DECRYPT_MODE);
    }

    private byte[] apply(byte[] input, int mode) {
        if (input == null || input.length != 8) {
            throw new IllegalArgumentException("PIN block must be exactly 8 bytes");
        }

        try {
            Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
            cipher.init(mode, key);
            return cipher.doFinal(input);
        } catch (GeneralSecurityException e) {
            throw new PinVerificationInfrastructureException("Simulator PIN-block cryptographic operation failed", e);
        }
    }
}
