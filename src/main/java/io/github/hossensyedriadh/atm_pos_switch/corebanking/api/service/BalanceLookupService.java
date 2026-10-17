package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingAccountBalanceResponse;

public interface BalanceLookupService {
    CoreBankingAccountBalanceResponse lookupBalance(Long accountId);
}
