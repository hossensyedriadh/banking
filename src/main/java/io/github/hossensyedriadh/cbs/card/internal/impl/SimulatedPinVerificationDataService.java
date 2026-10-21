package io.github.hossensyedriadh.cbs.card.internal.impl;

import io.github.hossensyedriadh.cbs.card.api.service.PinVerificationDataService;
import io.github.hossensyedriadh.crypto_commons.pin.exception.PinVerificationInfrastructureException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class SimulatedPinVerificationDataService implements PinVerificationDataService {
    private SecretKeySpec key;

    @Value("${card.security.crypto.pin-simulator.verification-key-base64}")
    private String encodedKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(this.encodedKey);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("Verification key must be at least 32 bytes long");
        }

        this.key = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Override
    public String createVerificationData(String pan, String pin) {
        this.validate(pan, pin);
        return this.calculateToken(pan, pin);
    }

    public boolean matches(String pan, String pin, String storedToken) {
        if (storedToken == null || !storedToken.matches("[0-9a-f]{64}")) {
            return false;
        }

        this.validate(pan, pin);

        byte[] expected = this.calculateToken(pan, pin).getBytes(StandardCharsets.US_ASCII);
        byte[] actual = storedToken.getBytes(StandardCharsets.US_ASCII);

        return MessageDigest.isEqual(expected, actual);
    }

    private String calculateToken(String pan, String pin) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);

            byte[] input = (pan + ":" + pin).getBytes(StandardCharsets.UTF_8);

            return HexFormat.of().formatHex(mac.doFinal(input));
        } catch (GeneralSecurityException e) {
            throw new PinVerificationInfrastructureException("Simulated PIN verification failed", e);
        }
    }

    private void validate(String pan, String pin) {
        if (pan == null || !pan.matches("\\d{13,19}")) {
            throw new IllegalArgumentException("Invalid PAN");
        }

        if (pin == null || !pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("Simulated currently supports only four-digit PINs");
        }
    }
}
