package io.github.hossensyedriadh.cbs.account.api.validation;

import io.github.hossensyedriadh.cbs.account.internal.validator.FeeRuleValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FeeRuleValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFeeRule {
    String message() default "Invalid fee rule";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
