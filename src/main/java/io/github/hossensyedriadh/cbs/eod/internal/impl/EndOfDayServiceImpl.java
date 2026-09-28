package io.github.hossensyedriadh.cbs.eod.internal.impl;

import io.github.hossensyedriadh.cbs.eod.api.service.BusinessDateService;
import io.github.hossensyedriadh.cbs.eod.api.service.EndOfDayService;
import io.github.hossensyedriadh.cbs.eod.internal.domain.EodRun;
import io.github.hossensyedriadh.cbs.eod.internal.domain.EodRunStep;
import io.github.hossensyedriadh.cbs.eod.internal.service.EndOfDayStep;
import io.github.hossensyedriadh.cbs.eod.internal.service.EodRunService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Log4j2
@Service
public class EndOfDayServiceImpl implements EndOfDayService {
    private final BusinessDateService businessDateService;
    private final EodRunService eodRunService;
    private final List<EndOfDayStep> steps;

    @Autowired
    public EndOfDayServiceImpl(BusinessDateService businessDateService, EodRunService eodRunService, List<EndOfDayStep> steps) {
        this.businessDateService = businessDateService;
        this.eodRunService = eodRunService;
        this.steps = steps.stream().sorted(Comparator.comparing(step -> step.getStep().getOrder()))
                .toList();
    }

    @Override
    public void run() {
        LocalDate businessDate = this.businessDateService.getCurrentBusinessDate();
        String formattedBusinessDate = businessDate.format(DateTimeFormatter.ISO_DATE);

        log.info("Preparing for EOD run for business date: {}", formattedBusinessDate);

        EodRun run = this.eodRunService.start(businessDate);

        try {
            steps.forEach(step -> {
                EodRunStep runStep = this.eodRunService.startStep(run, step.getStep());

                try {
                    log.info("Executing EOD run step: {}, business date: {}", runStep.getStepName().toString(), formattedBusinessDate);
                    step.execute(this.businessDateService.getCurrentBusinessDate());

                    this.eodRunService.completeStep(runStep);

                    log.info("EOD run step: {} completed, business date: {}", runStep.getStepName().toString(), formattedBusinessDate);
                } catch (Exception e) {
                    this.eodRunService.failStep(runStep, e.getMessage());
                    log.error("Exception while executing EOD run step: {} for business date: {}", step.getStep().toString(), formattedBusinessDate, e);
                    throw e;
                }
            });

            this.businessDateService.rolloverBusinessDate();
            this.eodRunService.complete(run);

            log.info("{} EOD steps completed successfully for business date: {}", this.steps.size(), formattedBusinessDate);
        } catch (Exception e) {
            this.eodRunService.fail(run, e.getMessage());
            log.error("Exception occurred while running EOD for business date: {}", formattedBusinessDate, e);
            throw e;
        }
    }
}
