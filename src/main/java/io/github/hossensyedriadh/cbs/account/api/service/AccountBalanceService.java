package io.github.hossensyedriadh.cbs.account.api.service;

import io.github.hossensyedriadh.cbs.account.api.model.AccountBalanceView;

import java.util.Optional;

public interface AccountBalanceService {
    Optional<AccountBalanceView> getBalance(Long accountId);
}
