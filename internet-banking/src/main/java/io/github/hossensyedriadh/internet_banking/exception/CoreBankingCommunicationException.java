package io.github.hossensyedriadh.internet_banking.exception;

import lombok.Getter;

import java.io.Serial;

@Getter
public class CoreBankingCommunicationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -7510236679436435227L;

    public CoreBankingCommunicationException(String message) {
        super(message);
    }

    public CoreBankingCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
