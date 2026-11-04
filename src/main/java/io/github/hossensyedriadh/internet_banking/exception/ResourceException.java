package io.github.hossensyedriadh.internet_banking.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.io.Serial;

@Getter
@Setter
public class ResourceException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 866880912668588716L;

    private HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    public ResourceException(String message) {
        super(message);
    }

    public ResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public ResourceException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }
}
