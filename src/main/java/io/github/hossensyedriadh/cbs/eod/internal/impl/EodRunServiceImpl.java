package io.github.hossensyedriadh.cbs.eod.internal.impl;

import io.github.hossensyedriadh.cbs.eod.api.enumerator.EodRunStatus;
import io.github.hossensyedriadh.cbs.eod.api.enumerator.EodStep;
import io.github.hossensyedriadh.cbs.eod.api.enumerator.EodStepStatus;
import io.github.hossensyedriadh.cbs.eod.internal.domain.EodRun;
import io.github.hossensyedriadh.cbs.eod.internal.domain.EodRunStep;
import io.github.hossensyedriadh.cbs.eod.internal.repository.EodRunRepository;
import io.github.hossensyedriadh.cbs.eod.internal.repository.EodRunStepRepository;
import io.github.hossensyedriadh.cbs.eod.internal.service.EodRunService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Log4j2
@Service
public class EodRunServiceImpl implements EodRunService {
    private final EodRunRepository eodRunRepository;
    private final EodRunStepRepository eodRunStepRepository;

    @Autowired
    public EodRunServiceImpl(EodRunRepository eodRunRepository, EodRunStepRepository eodRunStepRepository) {
        this.eodRunRepository = eodRunRepository;
        this.eodRunStepRepository = eodRunStepRepository;
    }

    @Transactional
    @Override
    public EodRun start(LocalDate businessDate) {
        EodRun eodRun = new EodRun();
        eodRun.setBusinessDate(businessDate);
        eodRun.setStatus(EodRunStatus.RUNNING);

        EodRun saved = this.eodRunRepository.save(eodRun);

        log.info("Started EodRun with id: {}", saved.getId());

        return saved;
    }

    @Transactional
    @Override
    public void complete(EodRun eodRun) {
        eodRun.setStatus(EodRunStatus.COMPLETED);
        eodRun.setCompletedAt(OffsetDateTime.now());

        this.eodRunRepository.save(eodRun);

        log.info("Completed EodRun with id: {}", eodRun.getId());
    }

    @Transactional
    @Override
    public void fail(EodRun eodRun, String errorMessage) {
        eodRun.setStatus(EodRunStatus.FAILED);
        eodRun.setErrorMessage(errorMessage);
        eodRun.setCompletedAt(OffsetDateTime.now());

        this.eodRunRepository.save(eodRun);

        log.warn("Failed EodRun with id: {}", eodRun.getId());
    }

    @Transactional
    @Override
    public EodRunStep startStep(EodRun eodRun, EodStep eodStep) {
        EodRunStep eodRunStep = new EodRunStep();
        eodRunStep.setEodRun(eodRun);
        eodRunStep.setStepName(eodStep);
        eodRunStep.setStepOrder(eodStep.getOrder());
        eodRunStep.setStatus(EodStepStatus.RUNNING);
        eodRunStep.setStartedAt(OffsetDateTime.now());

        EodRunStep saved = this.eodRunStepRepository.save(eodRunStep);

        log.info("Started EodRunStep with id: {}", saved.getId());

        return saved;
    }

    @Transactional
    @Override
    public void completeStep(EodRunStep eodRunStep) {
        eodRunStep.setStatus(EodStepStatus.COMPLETED);
        eodRunStep.setCompletedAt(OffsetDateTime.now());

        this.eodRunStepRepository.save(eodRunStep);

        log.info("Completed EodRunStep with id: {}", eodRunStep.getId());
    }

    @Transactional
    @Override
    public void failStep(EodRunStep eodRunStep, String errorMessage) {
        eodRunStep.setStatus(EodStepStatus.FAILED);
        eodRunStep.setErrorMessage(errorMessage);
        eodRunStep.setCompletedAt(OffsetDateTime.now());

        this.eodRunStepRepository.save(eodRunStep);

        log.warn("Failed EodRunStep with id: {}", eodRunStep.getId());
    }
}
