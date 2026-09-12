package io.github.hossensyedriadh.cbs.account.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.AccountBalanceService;
import io.github.hossensyedriadh.cbs.account.api.AccountService;
import io.github.hossensyedriadh.cbs.account.api.model.AccountBalanceView;
import io.github.hossensyedriadh.cbs.ledger.api.LedgerService;
import io.github.hossensyedriadh.cbs.ledger.api.model.SubLedgerAccountView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Log4j2
@Service
@Transactional(readOnly = true)
public class AccountBalanceServiceImpl implements AccountBalanceService {
    private final AccountService accountService;
    private final LedgerService ledgerService;

    @Autowired
    public AccountBalanceServiceImpl(AccountService accountService, LedgerService ledgerService) {
        this.accountService = accountService;
        this.ledgerService = ledgerService;
    }

    @Override
    public Optional<AccountBalanceView> getBalance(Long accountId) {
        return this.accountService.getAccount(accountId).map(account -> {
            BigDecimal balance = this.ledgerService.getSubLedgerAccountByAccountId(accountId)
                    .map(SubLedgerAccountView::getBalance).orElse(BigDecimal.ZERO);

            return new AccountBalanceView(accountId, balance, balance);
        });
    }
}
