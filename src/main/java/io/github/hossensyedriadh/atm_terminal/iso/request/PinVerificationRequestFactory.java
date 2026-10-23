package io.github.hossensyedriadh.atm_terminal.iso.request;

import io.github.hossensyedriadh.atm_terminal.configuration.properties.AtmTerminalProperties;
import io.github.hossensyedriadh.atm_terminal.util.ProcessingCodes;
import io.github.hossensyedriadh.crypto_commons.pin.cipher.SimulatedPinBlockCipher;
import io.github.hossensyedriadh.crypto_commons.pin.codec.Iso0PinBlockCodec;
import jakarta.annotation.PostConstruct;
import org.jpos.iso.ISOMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class PinVerificationRequestFactory {
    private static final DateTimeFormatter TRANSMISSION_DATE_TIME = DateTimeFormatter.ofPattern("MMddHHmmss");

    private SimulatedPinBlockCipher simulatedPinBlockCipher;
    private final AtmTerminalProperties atmTerminalProperties;
    private final Clock clock;

    @Value("${atm.security.pin-block.encryption-secret-key}")
    private String secretKey;

    @Autowired
    public PinVerificationRequestFactory(AtmTerminalProperties atmTerminalProperties) {
        this.atmTerminalProperties = atmTerminalProperties;
        this.clock = Clock.system(ZoneId.of("Asia/Dhaka"));
    }

    @PostConstruct
    private void init() {
        this.simulatedPinBlockCipher = new SimulatedPinBlockCipher(secretKey);
    }

    public ISOMsg create(String pan, String pin) {
        byte[] pinBlock = Iso0PinBlockCodec.encode(pan, pin);
        byte[] encryptedPinBlock = this.simulatedPinBlockCipher.encrypt(pinBlock);

        ISOMsg request = new ISOMsg("0100");
        request.set(2, pan);
        request.set(3, ProcessingCodes.PIN_VERIFICATION);
        request.set(7, LocalDateTime.now(this.clock).format(TRANSMISSION_DATE_TIME));
        request.set(11, "%06d".formatted(ThreadLocalRandom.current().nextInt(1_000_000)));
        request.set(41, this.atmTerminalProperties.terminalId());
        request.set(52, encryptedPinBlock);

        return request;
    }
}
