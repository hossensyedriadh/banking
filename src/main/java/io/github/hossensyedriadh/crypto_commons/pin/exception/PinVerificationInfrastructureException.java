package io.github.hossensyedriadh.crypto_commons.pin.exception;

import java.io.Serial;

public class PinVerificationInfrastructureException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -4621673542498066379L;

    public PinVerificationInfrastructureException(String message) {
        super(message);
    }

    public PinVerificationInfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
