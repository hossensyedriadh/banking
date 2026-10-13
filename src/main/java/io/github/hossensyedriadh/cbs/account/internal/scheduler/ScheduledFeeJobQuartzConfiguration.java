package io.github.hossensyedriadh.cbs.account.internal.scheduler;

import io.github.hossensyedriadh.cbs.account.internal.job.ScheduledFeeQuartzJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
public class ScheduledFeeJobQuartzConfiguration {
    public static final String JOB_NAME = "SCHEDULED_FEE_PROCESSING_JOB";
    public static final String TRIGGER_NAME = "SCHEDULED_FEE_PROCESSING_TRIGGER";

    @Value("${cbs.timezone}")
    private ZoneId cbsTimezone;

    @Bean
    public JobDetail scheduledFeeJobDetail() {
        return JobBuilder.newJob(ScheduledFeeQuartzJob.class)
                .withIdentity(JOB_NAME)
                .withDescription("Processes scheduled customer fees")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger scheduledFeeTrigger(JobDetail jobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(TRIGGER_NAME)
                .withDescription("Runs scheduled-fee processing every 1 hour")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0/1 * * ?")
                        .withMisfireHandlingInstructionDoNothing()
                        .inTimeZone(TimeZone.getTimeZone(this.cbsTimezone))
                )
                .build();
    }
}
