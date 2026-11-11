package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingAccountProductResponse;

public interface AccountProductLookupService {
    CoreBankingAccountProductResponse lookupAccountProduct(Long id);
}
