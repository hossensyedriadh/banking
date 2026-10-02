package io.github.hossensyedriadh.cbs.model;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;

@Getter
public class ValidationErrorResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 7425168331888252585L;

    private final int status;

    private final OffsetDateTime timestamp;

    private final String message;

    private final String error;

    private final String path;

    private final HashMap<String, String> details;

    public ValidationErrorResponse(HttpServletRequest httpServletRequest, Integer httpStatus, String error, String message, HashMap<String, String> details) {
        this.timestamp = OffsetDateTime.now(ZoneOffset.UTC);
        this.status = httpStatus;
        this.error = error;
        this.message = message;
        this.details = details;
        this.path = httpServletRequest.getRequestURI();
    }
}
