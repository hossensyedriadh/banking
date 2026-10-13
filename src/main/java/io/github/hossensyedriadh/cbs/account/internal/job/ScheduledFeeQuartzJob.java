package io.github.hossensyedriadh.cbs.account.internal.job;

import io.github.hossensyedriadh.cbs.account.api.service.ScheduledFeeService;
import io.github.hossensyedriadh.cbs.eod.api.service.BusinessDateService;
import lombok.extern.log4j.Log4j2;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Log4j2
@DisallowConcurrentExecution
public class ScheduledFeeQuartzJob implements Job {
    private final BusinessDateService businessDateService;
    private final ScheduledFeeService scheduledFeeService;

    public ScheduledFeeQuartzJob(BusinessDateService businessDateService, ScheduledFeeService scheduledFeeService) {
        this.businessDateService = businessDateService;
        this.scheduledFeeService = scheduledFeeService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            LocalDate businessDate = this.businessDateService.getCurrentBusinessDate();

            this.scheduledFeeService.processDueFees(businessDate);

            log.info("Completed scheduled fee Quartz job for business date: {}", businessDate.format(DateTimeFormatter.ISO_DATE));
        } catch (Exception e) {
            log.error("Scheduled fee Quartz job failed", e);
            throw new JobExecutionException(e);
        }
    }
}
