package io.github.hossensyedriadh.cbs.account.api.service;

import io.github.hossensyedriadh.cbs.account.api.command.CloseAccountCommand;
import io.github.hossensyedriadh.cbs.account.api.command.CreateAccountCommand;
import io.github.hossensyedriadh.cbs.account.api.model.AccountView;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

public interface AccountService {
    List<AccountView> getAccountsByCustomerId(Long customerId);

    List<AccountView> getActiveAccountsByCustomerId(Long customerId);

    List<AccountView> getActiveAccounts();

    Optional<AccountView> getAccount(Long id);

    Optional<AccountView> getPrincipalAccount(Long id);

    Optional<AccountView> getAccountByNumber(String accountNumber);

    Optional<AccountView> getAccountForUpdate(Long id);

    Optional<AccountView> getActiveAccountByAccountIdAndCustomerId(Long accountId, Long customerId);

    AccountView createAccount(@Valid CreateAccountCommand command);

    void activateAccount(Long id);

    void deactivateAccount(Long id);

    AccountView closeAccount(@Valid CloseAccountCommand command);
}
