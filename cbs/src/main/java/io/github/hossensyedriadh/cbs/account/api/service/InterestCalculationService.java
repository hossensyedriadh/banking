package io.github.hossensyedriadh.cbs.account.api.service;

import java.math.BigDecimal;

public interface InterestCalculationService {
    BigDecimal calculateDailyInterest(BigDecimal balance, BigDecimal interestRate);
}
