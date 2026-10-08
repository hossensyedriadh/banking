package io.github.hossensyedriadh.cbs.account.api.service;

import io.github.hossensyedriadh.cbs.account.api.model.InterestAccrualView;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface InterestService {
    void accrue(LocalDate businessDate);

    List<InterestAccrualView> getNotPostedAccrualsForMonth(YearMonth yearMonth);

    InterestAccrualView post(Long accrualId);
}
