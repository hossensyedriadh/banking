package io.github.hossensyedriadh.cbs.account.internal.validator;

import io.github.hossensyedriadh.cbs.account.api.model.CreateFeeRuleCommand;
import io.github.hossensyedriadh.cbs.account.api.validation.ValidFeeRule;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FeeRuleValidator implements ConstraintValidator<ValidFeeRule, CreateFeeRuleCommand> {
    @Override
    public boolean isValid(CreateFeeRuleCommand command, ConstraintValidatorContext context) {
        if (command == null || command.getFeeType() == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        switch (command.getFeeType()) {
            case FIXED -> {
                if (command.getFixedAmount() == null) {
                    context.buildConstraintViolationWithTemplate("Fixed amount is required for a fixed fee")
                            .addPropertyNode("fixedAmount")
                            .addConstraintViolation();

                    return false;
                }

                if (command.getPercentage() != null) {
                    context.buildConstraintViolationWithTemplate("Percentage must not be provided for a fixed fee")
                            .addPropertyNode("percentage")
                            .addConstraintViolation();

                    return false;
                }
            }

            case PERCENTAGE -> {
                if (command.getPercentage() == null) {
                    context.buildConstraintViolationWithTemplate("Percentage is required for a percentage fee")
                            .addPropertyNode("percentage")
                            .addConstraintViolation();

                    return false;
                }

                if (command.getFixedAmount() != null) {
                    context.buildConstraintViolationWithTemplate("Fixed amount must not be provided for a percentage fee")
                            .addPropertyNode("fixedAmount")
                            .addConstraintViolation();

                    return false;
                }
            }
        }

        return true;
    }
}
