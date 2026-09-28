package io.github.hossensyedriadh.cbs.eod.internal.impl;

import io.github.hossensyedriadh.cbs.eod.api.enumerator.EodRunStatus;
import io.github.hossensyedriadh.cbs.eod.api.enumerator.EodStepStatus;
import io.github.hossensyedriadh.cbs.eod.api.model.EodRunResult;
import io.github.hossensyedriadh.cbs.eod.api.model.EodStepResult;
import io.github.hossensyedriadh.cbs.eod.api.service.BusinessDateService;
import io.github.hossensyedriadh.cbs.eod.api.service.EndOfDayService;
import io.github.hossensyedriadh.cbs.eod.internal.domain.EodRun;
import io.github.hossensyedriadh.cbs.eod.internal.domain.EodRunStep;
import io.github.hossensyedriadh.cbs.eod.internal.service.EndOfDayStep;
import io.github.hossensyedriadh.cbs.eod.internal.service.EodRunService;
import io.github.hossensyedriadh.cbs.eod.internal.util.DistributedLockKeys;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class EndOfDayServiceImpl implements EndOfDayService {
    private final BusinessDateService businessDateService;
    private final EodRunService eodRunService;
    private final RedissonClient redissonClient;
    private final List<EndOfDayStep> steps;

    @Autowired
    public EndOfDayServiceImpl(BusinessDateService businessDateService, EodRunService eodRunService,
                               RedissonClient redissonClient, List<EndOfDayStep> steps) {
        this.businessDateService = businessDateService;
        this.eodRunService = eodRunService;
        this.redissonClient = redissonClient;
        this.steps = steps.stream().sorted(Comparator.comparing(step -> step.getStep().getOrder())).toList();
    }

    @Override
    public EodRunResult run() throws InterruptedException {
        RLock lock = this.redissonClient.getLock(DistributedLockKeys.EOD_RUN.getKey());

        boolean acquired = lock.tryLock(0, 1, TimeUnit.HOURS);

        if (!acquired) {
            log.warn("Another instance is already handling EOD lock: {}", DistributedLockKeys.EOD_RUN.getKey());
            throw new IllegalStateException("Another instance is already running EOD");
        }

        try {
            LocalDate businessDate = this.businessDateService.getCurrentBusinessDate();

            String formattedBusinessDate = businessDate.format(DateTimeFormatter.ISO_DATE);

            log.info("Preparing EOD run for business date: {}", formattedBusinessDate);

            EodRun run = this.eodRunService.start(businessDate);

            List<EodStepResult> stepResults = new ArrayList<>();

            for (EndOfDayStep step : this.steps) {
                EodRunStep runStep = this.eodRunService.startStep(run, step.getStep());

                try {
                    log.info("Executing EOD step: {}, business date: {}", step.getStep(), formattedBusinessDate);

                    step.execute(businessDate);

                    this.eodRunService.completeStep(runStep);

                    stepResults.add(EodStepResult.builder().step(step.getStep()).status(EodStepStatus.COMPLETED).build());

                    log.info("EOD step {} completed successfully for business date: {}", step.getStep(), formattedBusinessDate);
                } catch (Exception e) {
                    this.eodRunService.failStep(runStep, e.getMessage());

                    log.error("EOD step {} failed for business date: {}", step.getStep(), formattedBusinessDate, e);

                    stepResults.add(EodStepResult.builder().step(step.getStep()).status(EodStepStatus.FAILED).errorMessage(e.getMessage()).build());

                    this.eodRunService.fail(run, "EOD step " + step.getStep() + " failed");

                    this.steps.stream().dropWhile(remainingStep -> remainingStep != step)
                            .skip(1).forEach(remainingStep ->
                                    stepResults.add(EodStepResult.builder().step(remainingStep.getStep()).status(EodStepStatus.PENDING).build())
                            );

                    return EodRunResult.builder().runId(run.getId()).businessDate(businessDate).status(EodRunStatus.FAILED).steps(stepResults).build();
                }
            }

            this.businessDateService.rolloverBusinessDate();
            this.eodRunService.complete(run);

            log.info("{} EOD steps completed successfully for business date: {}", this.steps.size(), formattedBusinessDate);

            return EodRunResult.builder().runId(run.getId()).businessDate(businessDate).status(EodRunStatus.COMPLETED).steps(stepResults).build();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
