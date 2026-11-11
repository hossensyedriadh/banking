package io.github.hossensyedriadh.internet_banking.exception;

import java.io.Serial;

public class CoreBankingException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -2283441682586427998L;

    public CoreBankingException(String message) {
        super(message);
    }

    public CoreBankingException(String message, Throwable cause) {
        super(message, cause);
    }
}
