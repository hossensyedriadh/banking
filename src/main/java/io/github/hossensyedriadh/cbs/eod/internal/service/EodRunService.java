package io.github.hossensyedriadh.cbs.eod.internal.service;

import io.github.hossensyedriadh.cbs.eod.api.enumerator.EodStep;
import io.github.hossensyedriadh.cbs.eod.internal.domain.EodRun;
import io.github.hossensyedriadh.cbs.eod.internal.domain.EodRunStep;

import java.time.LocalDate;

public interface EodRunService {
    EodRun start(LocalDate businessDate);

    void complete(EodRun eodRun);

    void fail(EodRun eodRun, String errorMessage);

    EodRunStep startStep(EodRun eodRun, EodStep eodStep);

    void completeStep(EodRunStep eodRunStep);

    void failStep(EodRunStep eodRunStep, String errorMessage);
}
