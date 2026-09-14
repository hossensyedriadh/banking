package io.github.hossensyedriadh.cbs.account.api;

import java.time.LocalDate;

public interface InterestBatchService {
    void processDailyInterest(LocalDate calculationDate);
}
