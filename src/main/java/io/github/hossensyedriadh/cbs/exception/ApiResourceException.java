package io.github.hossensyedriadh.cbs.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.io.Serial;

@Getter
@Setter
public class ApiResourceException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -496926192392473588L;

    private HttpStatus httpStatus;

    private HttpServletRequest httpServletRequest;

    public ApiResourceException(String message) {
        super(message);
    }

    public ApiResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiResourceException(String message, HttpStatus httpStatus, HttpServletRequest httpServletRequest) {
        super(message);
        this.httpStatus = httpStatus;
        this.httpServletRequest = httpServletRequest;
    }

    public ApiResourceException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
