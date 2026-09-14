package io.github.hossensyedriadh.cbs.account.api;

import io.github.hossensyedriadh.cbs.account.api.model.InterestAccrualView;

import java.time.LocalDate;

public interface InterestAccrualService {
    InterestAccrualView accrue(Long accountId, LocalDate calculationDate);
}
