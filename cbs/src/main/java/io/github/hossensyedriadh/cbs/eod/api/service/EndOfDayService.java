package io.github.hossensyedriadh.cbs.eod.api.service;

import io.github.hossensyedriadh.cbs.eod.api.model.EodRunResult;

public interface EndOfDayService {
    EodRunResult run() throws InterruptedException;
}
