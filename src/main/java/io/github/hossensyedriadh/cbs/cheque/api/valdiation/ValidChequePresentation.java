package io.github.hossensyedriadh.cbs.cheque.api.valdiation;

import io.github.hossensyedriadh.cbs.cheque.internal.validator.ChequePresentationValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ChequePresentationValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidChequePresentation {
    String message() default "{ValidChequePresentation.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

