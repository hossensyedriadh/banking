package io.github.hossensyedriadh.cbs.cheque.internal.validator;

import io.github.hossensyedriadh.cbs.cheque.api.command.PresentChequeCommand;
import io.github.hossensyedriadh.cbs.cheque.api.enumerator.ChequePresentationType;
import io.github.hossensyedriadh.cbs.cheque.api.valdiation.ValidChequePresentation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ChequePresentationValidator implements ConstraintValidator<ValidChequePresentation, PresentChequeCommand> {
    @Override
    public boolean isValid(PresentChequeCommand command, ConstraintValidatorContext context) {
        if (command == null || command.getPresentationType() == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        if (command.getPresentationType() == ChequePresentationType.ACCOUNT_CREDIT) {
            if (command.getPresentingBankCode() == null || command.getPresentingBankCode().isEmpty()) {
                context.buildConstraintViolationWithTemplate("Valid bank code is required")
                        .addPropertyNode("presentingBankCode")
                        .addConstraintViolation();
            }

            if (command.getPresentingAccountNo()  == null || command.getPresentingAccountNo().isEmpty()) {
                context.buildConstraintViolationWithTemplate("Valid account number is required")
                        .addPropertyNode("presentingAccountNo")
                        .addConstraintViolation();
            }
        }

        return true;
    }
}
