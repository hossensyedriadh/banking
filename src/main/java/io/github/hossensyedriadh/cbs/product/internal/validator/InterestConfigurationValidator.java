package io.github.hossensyedriadh.cbs.product.internal.validator;

import io.github.hossensyedriadh.cbs.product.api.command.CreateAccountProductCommand;
import io.github.hossensyedriadh.cbs.product.api.validation.ValidInterestConfiguration;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class InterestConfigurationValidator implements ConstraintValidator<ValidInterestConfiguration, CreateAccountProductCommand> {
    @Override
    public boolean isValid(CreateAccountProductCommand cmd, ConstraintValidatorContext context) {
        if (cmd == null || !cmd.isInterestBearing()) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        if (cmd.getInterestRate() == null) {
            context.buildConstraintViolationWithTemplate("Interest rate is required")
                    .addPropertyNode("interestRate")
                    .addConstraintViolation();

            return false;
        }

        if (cmd.getInterestRate().signum() < 0) {
            context.buildConstraintViolationWithTemplate("Interest rate must be greater than or equal to zero")
                    .addPropertyNode("interestRate")
                    .addConstraintViolation();

            return false;
        }

        if (cmd.getInterestCalculationMethod() == null) {
            context.buildConstraintViolationWithTemplate("Interest calculation method is required")
                    .addPropertyNode("interestCalculationMethod")
                    .addConstraintViolation();

            return false;
        }

        return true;
    }
}
