package io.github.hossensyedriadh.cbs.account.api;

import io.github.hossensyedriadh.cbs.account.api.model.AccountView;
import io.github.hossensyedriadh.cbs.account.api.model.CloseAccountCommand;
import io.github.hossensyedriadh.cbs.account.api.model.CreateAccountCommand;

import java.util.List;
import java.util.Optional;

public interface AccountService {
    Optional<AccountView> getAccount(Long id);

    Optional<AccountView> getAccountForUpdate(Long id);

    Optional<AccountView> getAccountByNumber(String accountNumber);

    List<AccountView> getAccountByCustomerId(Long customerId);

    AccountView createAccount(CreateAccountCommand command);

    void blockAccount(Long id);

    void unblockAccount(Long id);

    AccountView closeAccount(CloseAccountCommand command);
}
