package io.github.hossensyedriadh.cbs.account.api.service;

import java.math.BigDecimal;

public interface FeeCalculationService {
    BigDecimal calculateFee(String feeCode, BigDecimal transactionAmount, Long currencyId);
}
