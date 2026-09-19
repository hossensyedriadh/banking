package io.github.hossensyedriadh.cbs.account.api.service;

import io.github.hossensyedriadh.cbs.account.api.model.InterestAccrualView;

import java.time.LocalDate;

public interface InterestAccrualService {
    InterestAccrualView accrue(Long accountId, LocalDate calculationDate);

    InterestAccrualView post(Long accrualId);
}
