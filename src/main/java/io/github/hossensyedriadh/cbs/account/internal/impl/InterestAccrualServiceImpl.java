package io.github.hossensyedriadh.cbs.account.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.AccountService;
import io.github.hossensyedriadh.cbs.account.api.InterestAccrualService;
import io.github.hossensyedriadh.cbs.account.api.InterestCalculationService;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountStatus;
import io.github.hossensyedriadh.cbs.account.api.enumerator.InterestAccrualStatus;
import io.github.hossensyedriadh.cbs.account.api.model.AccountView;
import io.github.hossensyedriadh.cbs.account.api.model.InterestAccrualView;
import io.github.hossensyedriadh.cbs.account.internal.domain.InterestAccrual;
import io.github.hossensyedriadh.cbs.account.internal.mapper.InterestAccrualMapper;
import io.github.hossensyedriadh.cbs.account.internal.repository.InterestAccrualRepository;
import io.github.hossensyedriadh.cbs.ledger.api.LedgerService;
import io.github.hossensyedriadh.cbs.ledger.api.model.SubLedgerAccountView;
import io.github.hossensyedriadh.cbs.product.api.AccountProductService;
import io.github.hossensyedriadh.cbs.product.api.model.AccountProductView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Log4j2
@Service
@Transactional(readOnly = true)
public class InterestAccrualServiceImpl implements InterestAccrualService {
    private final InterestAccrualRepository interestAccrualRepository;
    private final InterestCalculationService interestCalculationService;
    private final AccountService accountService;
    private final AccountProductService accountProductService;
    private final LedgerService ledgerService;
    private final InterestAccrualMapper interestAccrualMapper;

    @Autowired
    public InterestAccrualServiceImpl(InterestAccrualRepository interestAccrualRepository, InterestCalculationService interestCalculationService,
                                      AccountService accountService, AccountProductService accountProductService,
                                      LedgerService ledgerService, InterestAccrualMapper interestAccrualMapper) {
        this.interestAccrualRepository = interestAccrualRepository;
        this.interestCalculationService = interestCalculationService;
        this.accountService = accountService;
        this.accountProductService = accountProductService;
        this.ledgerService = ledgerService;
        this.interestAccrualMapper = interestAccrualMapper;
    }

    @Transactional
    @Override
    public InterestAccrualView accrue(Long accountId, LocalDate calculationDate) {
        InterestAccrual existingAccural = this.interestAccrualRepository.findByAccountIdAndCalculationDate(accountId, calculationDate)
                .orElse(null);

        if (existingAccural != null) {
            return this.interestAccrualMapper.toView(existingAccural);
        }

        AccountView account = this.accountService.getAccount(accountId)
                .orElseThrow(() -> {
                    log.warn("Account not found with id {}", accountId);
                    return new IllegalStateException("Account not found with id " + accountId);
                });

        if (account.getStatus() != AccountStatus.ACTIVE) {
            log.warn("Account with id {} is not active", accountId);
            throw new IllegalStateException("Account with id " + accountId + " is not active");
        }

        AccountProductView accountProduct = this.accountProductService.getProduct(account.getProductId())
                .orElseThrow(() -> {
                    log.warn("Product not found with id {}", account.getProductId());
                    return new IllegalStateException("Product not found with id " + account.getProductId());
                });

        if (!accountProduct.isInterestBearing()) {
            log.warn("Account with id {} is not interest bearing", accountId);
            throw new IllegalStateException("Account with id " + accountId + " is not interest bearing");
        }

        BigDecimal baseAmount = this.ledgerService.getSubLedgerAccountByAccountId(accountId)
                .map(SubLedgerAccountView::getBalance).orElseThrow(() -> {
                    log.warn("Sub ledger account not found with id {}", accountId);
                    return new IllegalStateException("Sub ledger account not found with id " + accountId);
                });

        BigDecimal interestAmount = this.interestCalculationService.calculateDailyInterest(baseAmount, accountProduct.getInterestRate());

        InterestAccrual accrual = new InterestAccrual();
        accrual.setAccountId(accountId);
        accrual.setCalculationDate(calculationDate);
        accrual.setBaseAmount(baseAmount);
        accrual.setInterestRate(accountProduct.getInterestRate());
        accrual.setInterestAmount(interestAmount);
        accrual.setStatus(InterestAccrualStatus.ACCRUED);

        InterestAccrual saved = this.interestAccrualRepository.save(accrual);

        log.info("Saved interest for product: {}, account: {}", accountProduct.getId(), accountId);

        return this.interestAccrualMapper.toView(saved);
    }
}
