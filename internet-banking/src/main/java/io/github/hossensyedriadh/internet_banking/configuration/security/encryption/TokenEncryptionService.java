package io.github.hossensyedriadh.internet_banking.configuration.security.encryption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class TokenEncryptionService {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int NONCE_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public TokenEncryptionService(@Value("${security.oauth2.token-encryption-key}") String encodedKey) {
        byte[] key = Base64.getDecoder().decode(encodedKey);

        if (key.length != 32) {
            throw new IllegalArgumentException("Token encryption key must be exactly 32 bytes");
        }

        this.secretKey = new SecretKeySpec(key, "AES");
    }

    public byte[] encrypt(String data, String context) {
        try {
            byte[] nonce = new byte[NONCE_LENGTH];
            this.secureRandom.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, this.secretKey, new GCMParameterSpec(TAG_LENGTH, nonce));
            cipher.updateAAD(context.getBytes(StandardCharsets.UTF_8));

            byte[] cipherText = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            byte[] result = new byte[nonce.length + cipherText.length];

            System.arraycopy(nonce, 0, result, 0, nonce.length);
            System.arraycopy(cipherText, 0, result, nonce.length, cipherText.length);

            return result;
        } catch (Exception e) {
            throw new IllegalStateException("Token encryption failed", e);
        }
    }

    public String decrypt(byte[] encrypted, String context) {
        try {
            if (encrypted == null || encrypted.length <= NONCE_LENGTH) {
                throw new IllegalArgumentException("Invalid encrypted token");
            }

            byte[] nonce = Arrays.copyOfRange(encrypted, 0, NONCE_LENGTH);
            byte[] cipherText = Arrays.copyOfRange(encrypted, NONCE_LENGTH, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, this.secretKey, new GCMParameterSpec(TAG_LENGTH, nonce));
            cipher.updateAAD(context.getBytes(StandardCharsets.UTF_8));

            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Token decryption failed", e);
        }
    }
}
