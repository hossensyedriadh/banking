package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingCardAccountAggregateResult;

public interface CardAccountLookupService {
    CoreBankingCardAccountAggregateResult lookupCardAccount(String pan);
}
