package io.github.hossensyedriadh.cbs.account.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.InterestAccrualService;
import io.github.hossensyedriadh.cbs.account.api.InterestBatchService;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountStatus;
import io.github.hossensyedriadh.cbs.account.api.model.InterestAccrualView;
import io.github.hossensyedriadh.cbs.account.internal.repository.AccountRepository;
import io.github.hossensyedriadh.cbs.product.api.enumerator.AccountProductStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Log4j2
@Service
// NOTE: no transactions should be opened in this class/any methods in this class
public class InterestBatchServiceImpl implements InterestBatchService {
    private final AccountRepository accountRepository;
    private final InterestAccrualService interestAccrualService;

    @Autowired
    public InterestBatchServiceImpl(AccountRepository accountRepository, InterestAccrualService interestAccrualService) {
        this.accountRepository = accountRepository;
        this.interestAccrualService = interestAccrualService;
    }

    @Override
    public void processDailyInterest(LocalDate calculationDate) {
        List<Long> accountIds = this.accountRepository.findEligibleInterestBearingAccountIds(AccountStatus.ACTIVE, AccountProductStatus.ACTIVE);

        String formattedDate = calculationDate.format(DateTimeFormatter.ISO_DATE);

        log.info("Processing started for daily interest for {} accounts on date: {}", accountIds.size(), formattedDate);

        int processedCount = 0;

        for (Long accountId : accountIds) {
            try {
                InterestAccrualView accrual = this.interestAccrualService.accrue(accountId, calculationDate);
                this.interestAccrualService.post(accrual.getId());
                processedCount++;

            } catch (Exception e) {
                log.error("Failed to process interest for account id {} on date: {}", accountId, formattedDate, e);
            }
        }

        int failedCount = accountIds.size() - processedCount;

        log.info("Daily interest processing completed. Processed: {}, Failed: {}, Date: {}", processedCount, failedCount, formattedDate);

        if (failedCount > 0) {
            log.warn("Daily interest processing failed for {} accounts on date: {}", failedCount, formattedDate);
        }
    }
}
