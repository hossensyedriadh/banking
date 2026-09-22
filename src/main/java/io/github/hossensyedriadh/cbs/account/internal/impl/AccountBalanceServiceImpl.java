package io.github.hossensyedriadh.cbs.account.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountHoldStatus;
import io.github.hossensyedriadh.cbs.account.api.model.AccountBalanceView;
import io.github.hossensyedriadh.cbs.account.api.service.AccountBalanceService;
import io.github.hossensyedriadh.cbs.account.api.service.AccountService;
import io.github.hossensyedriadh.cbs.account.internal.repository.AccountHoldRepository;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.ledger.api.model.SubLedgerAccountView;
import io.github.hossensyedriadh.cbs.ledger.api.service.LedgerService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Log4j2
@Service
@Transactional(readOnly = true)
public class AccountBalanceServiceImpl implements AccountBalanceService {
    private final AccountService accountService;
    private final LedgerService ledgerService;
    private final AccountHoldRepository accountHoldRepository;

    @Autowired
    public AccountBalanceServiceImpl(AccountService accountService, LedgerService ledgerService,
                                     AccountHoldRepository accountHoldRepository) {
        this.accountService = accountService;
        this.ledgerService = ledgerService;
        this.accountHoldRepository = accountHoldRepository;
    }

    @Override
    public AccountBalanceView getBalance(Long accountId) {
        this.accountService.getAccount(accountId).orElseThrow(() -> {
            log.warn("Account with id {} not found", accountId);
            return new ApiResourceException("Account not found", HttpStatus.BAD_REQUEST);
        });

        BigDecimal ledgerBalance = this.ledgerService.getSubLedgerAccountByAccountId(accountId)
                .map(SubLedgerAccountView::getBalance).orElse(BigDecimal.ZERO);

        BigDecimal heldAmount = this.accountHoldRepository.sumAmountByAccountIdAndStatus(accountId, AccountHoldStatus.ACTIVE);

        BigDecimal availableBalance = ledgerBalance.subtract(heldAmount);

        return new AccountBalanceView(accountId, ledgerBalance, heldAmount, availableBalance);
    }
}
