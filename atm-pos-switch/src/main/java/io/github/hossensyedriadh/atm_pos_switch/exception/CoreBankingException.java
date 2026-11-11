package io.github.hossensyedriadh.atm_pos_switch.exception;

import java.io.Serial;

public class CoreBankingException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 2330036866281113171L;

    public CoreBankingException(String message) {
        super(message);
    }

    public CoreBankingException(String message, Throwable cause) {
        super(message, cause);
    }
}
