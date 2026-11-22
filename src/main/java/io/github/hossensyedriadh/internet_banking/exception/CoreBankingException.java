package io.github.hossensyedriadh.internet_banking.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;

@Getter
public class CoreBankingException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -2283441682586427998L;

    private HttpStatus httpStatus;

    public CoreBankingException(String message) {
        super(message);
    }

    public CoreBankingException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public CoreBankingException(String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }
}
