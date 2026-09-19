package io.github.hossensyedriadh.cbs.account.internal.scheduler;

import io.github.hossensyedriadh.cbs.account.api.service.InterestBatchService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Log4j2
@Service
public class InterestBatchScheduler {
    private final InterestBatchService interestBatchService;

    @Autowired
    public InterestBatchScheduler(InterestBatchService interestBatchService) {
        this.interestBatchService = interestBatchService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void processDailyInterest() {
        LocalDate calculationDate = LocalDate.now().minusDays(1);

        log.info("Starting daily interest accrual/posting batch for date: {}", calculationDate.format(DateTimeFormatter.ISO_DATE));

        this.interestBatchService.processDailyInterest(calculationDate);
    }
}
