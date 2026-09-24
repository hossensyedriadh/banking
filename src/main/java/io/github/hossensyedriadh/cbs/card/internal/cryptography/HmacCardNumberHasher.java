package io.github.hossensyedriadh.cbs.card.internal.cryptography;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;

@Component
class HmacCardNumberHasher implements CardNumberHasher {
    private static final String ALGORITHM = "HmacSHA256";

    private final byte[] secret;

    public HmacCardNumberHasher(@Value("${card.security.crypto.pan-hmac-secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String hash(String cardNumber) {
        String normalized = this.normalize(cardNumber);

        try {
            Mac mac = Mac.getInstance(ALGORITHM);

            SecretKeySpec secretKey = new SecretKeySpec(secret, ALGORITHM);

            mac.init(secretKey);

            byte[] digest = mac.doFinal(normalized.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(digest);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to hash Card number", e);
        }
    }

    private String normalize(String cardNumber) {
        return cardNumber.replaceAll("\\s+", "");
    }
}
