package io.github.hossensyedriadh.cbs.account.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.model.InterestAccrualView;
import io.github.hossensyedriadh.cbs.account.api.service.InterestAccrualService;
import io.github.hossensyedriadh.cbs.account.api.service.InterestBatchService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Log4j2
@Service
// NOTE: no transactions should be opened in this class/any methods in this class
public class InterestBatchServiceImpl implements InterestBatchService {
    private final InterestAccrualService interestAccrualService;

    @Autowired
    public InterestBatchServiceImpl(InterestAccrualService interestAccrualService) {
        this.interestAccrualService = interestAccrualService;
    }

    @Override
    public void postInterests(LocalDate businessDate) {
        YearMonth previousMonth = YearMonth.from(businessDate).minusMonths(1);

        List<InterestAccrualView> accrualViews = this.interestAccrualService.getNotPostedAccrualsForMonth(previousMonth);

        log.info("Processing interest posting for {} accruals for accrual year-month: {}", accrualViews.size(), previousMonth);

        int postedCount = 0;

        for (InterestAccrualView accrual : accrualViews) {
            try {
                this.interestAccrualService.post(accrual.getId());
                postedCount++;
            } catch (Exception e) {
                log.error("Failed to process interest posting for accrual id {}", accrual.getId(), e);
            }
        }

        int failedCount = accrualViews.size() - postedCount;

        if (failedCount > 0) {
            log.warn("Interest posting failed for {} accruals, year-month: {}", failedCount, previousMonth);
        }

        log.info("Interest posting completed for accrual year-month: {}. Posted: {}, Failed: {}", previousMonth, postedCount, failedCount);
    }
}
