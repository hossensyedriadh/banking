package io.github.hossensyedriadh.atm_pos_switch.exception;

import java.io.Serial;

public class CoreBankingCommunicationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1292887865529167424L;

    public CoreBankingCommunicationException(String message) {
        super(message);
    }

    public CoreBankingCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
