package io.github.hossensyedriadh.cbs.eod.internal.service;

import io.github.hossensyedriadh.cbs.eod.api.enumerator.EodStep;

import java.time.LocalDate;

public interface EndOfDayStep {
    EodStep getStep();

    void execute(LocalDate businessDate);
}
