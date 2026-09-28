package io.github.hossensyedriadh.cbs.account.internal.scheduler;

import io.github.hossensyedriadh.cbs.account.api.service.InterestBatchService;
import io.github.hossensyedriadh.cbs.eod.api.service.BusinessDateService;
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
    private final BusinessDateService businessDateService;

    @Autowired
    public InterestBatchScheduler(InterestBatchService interestBatchService, BusinessDateService businessDateService) {
        this.interestBatchService = interestBatchService;
        this.businessDateService = businessDateService;
    }

    @Scheduled(cron = "0 0 0 1 * *")
    public void postInterests() {
        LocalDate businessDate = this.businessDateService.getCurrentBusinessDate();

        log.info("Starting interest posting for accruals, business date: {}", businessDate.format(DateTimeFormatter.ISO_DATE));

        this.interestBatchService.postInterests(businessDate);

        log.info("Monthly interest posting batch completed, business date: {}", businessDate.format(DateTimeFormatter.ISO_DATE));
    }
}
