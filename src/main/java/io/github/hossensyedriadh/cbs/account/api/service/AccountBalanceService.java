package io.github.hossensyedriadh.cbs.account.api.service;

import io.github.hossensyedriadh.cbs.account.api.model.AccountBalanceView;

public interface AccountBalanceService {
    AccountBalanceView getBalance(Long accountId);
}
