package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingCurrencyResponse;

public interface CurrencyLookupService {
    CoreBankingCurrencyResponse lookupCurrency(Long currencyId);
}
