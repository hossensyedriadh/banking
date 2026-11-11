package io.github.hossensyedriadh.internet_banking.account.service;

import io.github.hossensyedriadh.internet_banking.account.model.AccountOverviewModel;

import java.util.List;

public interface AccountService {
    List<AccountOverviewModel> overview();
}
