package io.github.hossensyedriadh.cbs.card.internal.impl;

import io.github.hossensyedriadh.cbs.card.api.command.PinBlockVerificationCommand;
import io.github.hossensyedriadh.cbs.card.api.service.PinBlockVerifier;
import io.github.hossensyedriadh.crypto_commons.pin.cipher.SimulatedPinBlockCipher;
import io.github.hossensyedriadh.crypto_commons.pin.codec.Iso0PinBlockCodec;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SimulatedPinBlockVerifier implements PinBlockVerifier {
    private SimulatedPinBlockCipher pinBlockCipher;
    private final SimulatedPinVerificationDataService pinVerificationDataService;

    @Value("${card.security.crypto.pin-simulator.tdes-key-base64}")
    private String secretKey;

    @Autowired
    public SimulatedPinBlockVerifier(SimulatedPinVerificationDataService pinVerificationDataService) {
        this.pinVerificationDataService = pinVerificationDataService;
    }

    @PostConstruct
    public void init() {
        this.pinBlockCipher = new SimulatedPinBlockCipher(this.secretKey);
    }

    @Override
    public boolean verify(PinBlockVerificationCommand command) {
        final byte[] clearPinBlock = this.pinBlockCipher.decrypt(command.encryptedPinBlock());

        final String enteredPin;

        try {
            enteredPin = Iso0PinBlockCodec.decode(command.pan(), clearPinBlock);
        } catch (IllegalArgumentException e) {
            return false;
        }

        if (enteredPin.length() != command.pinLength()) {
            return false;
        }

        return this.pinVerificationDataService.matches(command.pan(), enteredPin, command.pinVerificationData());
    }
}
