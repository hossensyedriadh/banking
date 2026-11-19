package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingAccountResponse;

public interface AccountLookupService {
    CoreBankingAccountResponse lookupAccount(String accountNumber);
}
