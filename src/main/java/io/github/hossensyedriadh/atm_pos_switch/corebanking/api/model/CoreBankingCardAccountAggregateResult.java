package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.enumerator.AccountStatus;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.enumerator.CardStatus;

import java.time.LocalDate;

public record CoreBankingCardAccountAggregateResult(Long cardId, Long cardProductId, String lastFour, CardStatus cardStatus,
                                                    LocalDate expiryDate, Long accountId, String accountNumber,
                                                    Long accountProductId, Long branchId, Long currencyId,
                                                    AccountStatus accountStatus) {
}
