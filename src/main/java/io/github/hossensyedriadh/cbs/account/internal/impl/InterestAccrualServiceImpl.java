package io.github.hossensyedriadh.cbs.account.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountStatus;
import io.github.hossensyedriadh.cbs.account.api.enumerator.InterestAccrualStatus;
import io.github.hossensyedriadh.cbs.account.api.model.AccountView;
import io.github.hossensyedriadh.cbs.account.api.model.InterestAccrualView;
import io.github.hossensyedriadh.cbs.account.api.service.AccountBalanceService;
import io.github.hossensyedriadh.cbs.account.api.service.AccountService;
import io.github.hossensyedriadh.cbs.account.api.service.InterestAccrualService;
import io.github.hossensyedriadh.cbs.account.api.service.InterestCalculationService;
import io.github.hossensyedriadh.cbs.account.internal.domain.InterestAccrual;
import io.github.hossensyedriadh.cbs.account.internal.mapper.InterestAccrualMapper;
import io.github.hossensyedriadh.cbs.account.internal.repository.InterestAccrualRepository;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.ledger.api.service.LedgerService;
import io.github.hossensyedriadh.cbs.product.api.enumerator.AccountProductStatus;
import io.github.hossensyedriadh.cbs.product.api.model.AccountProductView;
import io.github.hossensyedriadh.cbs.product.api.service.AccountProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Log4j2
@Service
@Transactional(readOnly = true)
public class InterestAccrualServiceImpl implements InterestAccrualService {
    private final InterestAccrualRepository interestAccrualRepository;
    private final InterestCalculationService interestCalculationService;
    private final AccountService accountService;
    private final AccountBalanceService accountBalanceService;
    private final AccountProductService accountProductService;
    private final LedgerService ledgerService;
    private final InterestAccrualMapper interestAccrualMapper;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public InterestAccrualServiceImpl(InterestAccrualRepository interestAccrualRepository, InterestCalculationService interestCalculationService,
                                      AccountService accountService, AccountBalanceService accountBalanceService,
                                      AccountProductService accountProductService, LedgerService ledgerService,
                                      InterestAccrualMapper interestAccrualMapper, HttpServletRequest httpServletRequest) {
        this.interestAccrualRepository = interestAccrualRepository;
        this.interestCalculationService = interestCalculationService;
        this.accountService = accountService;
        this.accountBalanceService = accountBalanceService;
        this.accountProductService = accountProductService;
        this.ledgerService = ledgerService;
        this.interestAccrualMapper = interestAccrualMapper;
        this.httpServletRequest = httpServletRequest;
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
                    return new ApiResourceException("Account not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
                });

        if (account.getStatus() != AccountStatus.ACTIVE) {
            log.warn("Account with id {} is not active", accountId);
            throw new ApiResourceException("Account is not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        AccountProductView accountProduct = this.accountProductService.getProduct(account.getProductId())
                .orElseThrow(() -> {
                    log.warn("Account Product not found with id {}", account.getProductId());
                    return new ApiResourceException("Account Product not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
                });

        if (accountProduct.getStatus() != AccountProductStatus.ACTIVE) {
            log.warn("Account Product with id {} is not active", accountId);
            throw new ApiResourceException("Account product is not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        if (!accountProduct.isInterestBearing()) {
            log.warn("Account with id {} is not interest bearing", accountId);
            throw new ApiResourceException("Account is not interest bearing", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        BigDecimal baseAmount = this.accountBalanceService.getBalance(accountId).getLedgerBalance();
        BigDecimal interestAmount = this.interestCalculationService.calculateDailyInterest(baseAmount, accountProduct.getInterestRate());

        InterestAccrual accrual = new InterestAccrual();
        accrual.setAccountId(accountId);
        accrual.setCalculationDate(calculationDate);
        accrual.setBaseAmount(baseAmount);
        accrual.setInterestRate(accountProduct.getInterestRate());
        accrual.setInterestAmount(interestAmount);
        accrual.setStatus(InterestAccrualStatus.ACCRUED);

        InterestAccrual saved = this.interestAccrualRepository.save(accrual);

        log.info("Accrued interest for account product: {}, account: {}", accountProduct.getId(), accountId);

        return this.interestAccrualMapper.toView(saved);
    }

    @Transactional
    @Override
    public InterestAccrualView post(Long accrualId) {
        InterestAccrual accrual = this.interestAccrualRepository.findByIdForUpdate(accrualId).orElseThrow(() -> {
            log.warn("Interest accrual not found with id {}", accrualId);
            return new ApiResourceException("Interest accrual not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (accrual.getStatus() != InterestAccrualStatus.ACCRUED) {
            log.warn("Interest accrual with id {} is not accrued", accrualId);
            throw new ApiResourceException("Interest accrual is not yet ACCRUED", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        Long customerLedgerAccountId = this.ledgerService.getSubLedgerAccountIdByAccountId(accrual.getAccountId()).orElseThrow(() -> {
            log.warn("Sub-ledger account not found for account id: {}", accrual.getAccountId());
            return new ApiResourceException("Sub-ledger account not found for account", HttpStatus.INTERNAL_SERVER_ERROR, this.httpServletRequest);
        });

        this.ledgerService.postInterest(accrual.getId(), customerLedgerAccountId, accrual.getInterestAmount());

        accrual.setStatus(InterestAccrualStatus.POSTED);
        accrual.setPostedAt(OffsetDateTime.now());

        InterestAccrual saved = this.interestAccrualRepository.save(accrual);

        log.info("Interest accrual posted with id: {}", saved.getId());

        return this.interestAccrualMapper.toView(saved);
    }
}
