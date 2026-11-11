package io.github.hossensyedriadh.atm_pos_switch.iso8583.validator;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.enumerator.AccountStatus;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.enumerator.CardStatus;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingCardAccountAggregateResult;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.util.ResponseCodes;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CardAccountValidator {
    public String validate(CoreBankingCardAccountAggregateResult aggregate) {
        if (aggregate == null) {
            return ResponseCodes.SYSTEM_ERROR;
        }

        if (aggregate.cardStatus() == null || aggregate.cardStatus() != CardStatus.ACTIVE) {
            return ResponseCodes.INVALID_CARD;
        }

        if (aggregate.expiryDate() == null || aggregate.expiryDate().isBefore(LocalDate.now())) {
            return ResponseCodes.EXPIRED_CARD;
        }

        if (aggregate.accountStatus() == null || aggregate.accountStatus() != AccountStatus.ACTIVE) {
            return ResponseCodes.INVALID_ACCOUNT;
        }

        return ResponseCodes.APPROVED;
    }
}
