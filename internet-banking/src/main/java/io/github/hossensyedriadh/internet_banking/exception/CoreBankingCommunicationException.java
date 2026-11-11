package io.github.hossensyedriadh.internet_banking.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;

@Getter
public class CoreBankingCommunicationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -7510236679436435227L;

    private final HttpStatus httpStatus;

    public CoreBankingCommunicationException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public CoreBankingCommunicationException(String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }
}
