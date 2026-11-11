package io.github.hossensyedriadh.internet_banking.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Getter
public class InternetBankingErrorResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1181487478595489403L;

    private final int status;

    private final OffsetDateTime timestamp;

    private final String message;

    private final String error;

    private final String path;

    public InternetBankingErrorResponse(HttpServletRequest httpServletRequest, Integer httpStatus, String error, String message) {
        this.timestamp = OffsetDateTime.now(ZoneOffset.UTC);
        this.status = httpStatus;
        this.error = error;
        this.message = message;
        this.path = httpServletRequest.getRequestURI();
    }

    public InternetBankingErrorResponse(HttpServletRequest httpServletRequest, Integer status, String message, Throwable throwable) {
        this.timestamp = OffsetDateTime.now(ZoneOffset.UTC);
        this.status = status;
        this.message = message;
        this.error = (throwable.getCause() != null) ? throwable.getCause().getMessage() : throwable.getMessage();
        this.path = httpServletRequest.getRequestURI();
    }
}
