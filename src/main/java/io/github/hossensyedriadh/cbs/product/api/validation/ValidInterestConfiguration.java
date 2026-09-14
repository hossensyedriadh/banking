package io.github.hossensyedriadh.cbs.product.api.validation;

import io.github.hossensyedriadh.cbs.product.internal.validator.InterestConfigurationValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Constraint(validatedBy = InterestConfigurationValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidInterestConfiguration {
    String message() default "Invalid interest configuration";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
