package io.github.hossensyedriadh.cbs.card.internal.validator;

import io.github.hossensyedriadh.cbs.card.api.validation.ValidCardPin;
import io.github.hossensyedriadh.cbs.card.internal.util.CardPinUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CardPinValidator implements ConstraintValidator<ValidCardPin, String> {
    @Override
    public boolean isValid(String pin, ConstraintValidatorContext context) {
        return CardPinUtil.isValidCardPin(pin);
    }
}
