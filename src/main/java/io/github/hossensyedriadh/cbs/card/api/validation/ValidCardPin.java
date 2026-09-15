package io.github.hossensyedriadh.cbs.card.api.validation;

import io.github.hossensyedriadh.cbs.card.internal.validator.CardPinValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CardPinValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCardPin {
    String message() default "Invalid card pin";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
