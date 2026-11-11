package io.github.hossensyedriadh.cbs.account.api.service;

import java.time.LocalDate;

public interface InterestBatchService {
    void postInterests(LocalDate businessDate);
}
