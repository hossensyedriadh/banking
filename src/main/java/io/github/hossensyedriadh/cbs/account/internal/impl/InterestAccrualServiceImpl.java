package io.github.hossensyedriadh.cbs.account.internal.impl;

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
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public void accrue(LocalDate businessDate) {
        List<AccountView> accountViews = this.accountService.getActiveAccounts();

        if (accountViews.isEmpty()) {
            log.info("No active interest-bearing accounts found for business date {}", businessDate);
            return;
        }

        List<Long> productIds = accountViews.stream().map(AccountView::getProductId).distinct().toList();

        Map<Long, AccountProductView> productsById = this.accountProductService.getActiveAndInterestBearingAccountProducts(productIds)
                .stream().collect(Collectors.toMap(AccountProductView::getId, Function.identity()));

        List<InterestAccrual> accruals = new ArrayList<>();

        for (AccountView account : accountViews) {
            AccountProductView product = productsById.get(account.getProductId());

            if (product == null) {
                log.warn("Skipping account {} because its interest-bearing product {} could not be found", account.getId(), account.getProductId());
                continue;
            }
            if (this.interestAccrualRepository.existsByAccountIdAndCalculationDate(account.getId(), businessDate)) {
                log.debug("Interest accrual already exists for account {} on {}", account.getId(), businessDate);
                continue;
            }

            BigDecimal baseAmount = this.accountBalanceService.getBalance(account.getId()).getLedgerBalance();
            BigDecimal interestAmount = this.interestCalculationService.calculateDailyInterest(baseAmount, product.getInterestRate());

            InterestAccrual accrual = new InterestAccrual();
            accrual.setAccountId(account.getId());
            accrual.setCalculationDate(businessDate);
            accrual.setBaseAmount(baseAmount);
            accrual.setInterestRate(product.getInterestRate());
            accrual.setInterestAmount(interestAmount);
            accrual.setStatus(InterestAccrualStatus.ACCRUED);

            accruals.add(accrual);
        }

        if (accruals.isEmpty()) {
            log.info("No new interest accruals generated for business date {}", businessDate);
            return;
        }

        this.interestAccrualRepository.saveAll(accruals);

        log.info("Generated {} interest accruals for business date {}", accruals.size(), businessDate);
    }

    @Override
    public List<InterestAccrualView> getNotPostedAccrualsForMonth(YearMonth yearMonth) {
        return this.interestAccrualRepository.findAllByStatusMonthAndYear(InterestAccrualStatus.ACCRUED, yearMonth.getMonthValue(), yearMonth.getYear())
                .stream().map(this.interestAccrualMapper::toView).toList();
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
