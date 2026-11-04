package io.github.hossensyedriadh.internet_banking.exceptionhandler.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.internet_banking.exception.InternetBankingErrorResponse;
import io.github.hossensyedriadh.internet_banking.exception.ResourceException;
import io.github.hossensyedriadh.internet_banking.exception.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;

@Log4j2
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = {"io.github.hossensyedriadh.internet_banking"})
public class InternetBankingExceptionHandler {
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public InternetBankingExceptionHandler(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<InternetBankingErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
        InternetBankingErrorResponse errorResponse = new InternetBankingErrorResponse(this.httpServletRequest, status.value(),
                status.getReasonPhrase(), e.getMessage());

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolationException(MethodArgumentNotValidException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        HashMap<String, String> errors = new HashMap<>();

        Object target = e.getTarget();

        if (target != null) {
            e.getAllErrors().forEach(error -> {
                if (error instanceof FieldError fieldError) {
                    try {
                        Class<?> beanClazz = target.getClass();
                        Field field = beanClazz.getDeclaredField(fieldError.getField());
                        var jsonPropertyAnnotation = field.getDeclaredAnnotation(JsonProperty.class);

                        if (jsonPropertyAnnotation != null) {
                            String jsonProperty = this.getJsonPropertyValue(beanClazz, field.getName());
                            errors.put(jsonProperty, fieldError.getDefaultMessage());
                        } else {
                            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
                        }
                    } catch (NoSuchFieldException ex) {
                        errors.put(fieldError.getField(), fieldError.getDefaultMessage());
                    }
                } else {
                    String object = error.getObjectName();
                    String message = error.getDefaultMessage();
                    errors.put(object, message);
                }
            });
        }

        ValidationErrorResponse validationErrorResponse = new ValidationErrorResponse(httpServletRequest, status.value(),
                status.getReasonPhrase(), String.format("Validation failed for %d fields/objects", e.getAllErrors().size()), errors);

        return new ResponseEntity<>(validationErrorResponse, status);
    }

    @ExceptionHandler(ResourceException.class)
    public ResponseEntity<?> handleResourceException(ResourceException e) {
        InternetBankingErrorResponse errorResponse = new InternetBankingErrorResponse(this.httpServletRequest, e.getHttpStatus().value(),
                e.getHttpStatus().getReasonPhrase(), e.getMessage());

        return new ResponseEntity<>(errorResponse, e.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String paramName = e.getName();
        String value = String.valueOf(e.getValue());
        String expectedType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown";

        String message = String.format("Invalid value '%s' for parameter '%s', Expected type: %s", value, paramName, expectedType);

        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        InternetBankingErrorResponse errorResponse = new InternetBankingErrorResponse(this.httpServletRequest, httpStatus.value(),
                httpStatus.getReasonPhrase(), message);

        return new ResponseEntity<>(errorResponse, httpStatus);
    }

    @ExceptionHandler({SQLException.class, NonTransientDataAccessException.class, TransactionSystemException.class})
    public ResponseEntity<?> handleSqlException(Exception e) {
        log.error("SQL|DAO Exception occurred: {}", e.getMessage(), e);

        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        InternetBankingErrorResponse errorResponse = new InternetBankingErrorResponse(this.httpServletRequest, httpStatus.value(),
                httpStatus.getReasonPhrase(), "Unexpected Server-Side exception");

        return new ResponseEntity<>(errorResponse, httpStatus);
    }

    @ExceptionHandler({Exception.class, NullPointerException.class})
    public ResponseEntity<?> handleRuntime(Exception e) {
        log.error("Exception occurred: {}", e.getMessage(), e);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        InternetBankingErrorResponse errorResponse = new InternetBankingErrorResponse(this.httpServletRequest, status.value(),
                status.getReasonPhrase(), "Unexpected Server-Side exception");

        return new ResponseEntity<>(errorResponse, status);
    }

    private String getJsonPropertyValue(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);

            return (jsonProperty != null && !jsonProperty.value().isEmpty()) ? jsonProperty.value() : fieldName;
        } catch (NoSuchFieldException e) {
            return fieldName;
        }
    }
}
