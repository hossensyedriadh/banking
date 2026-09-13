package io.github.hossensyedriadh.cbs.account.api;

import java.math.BigDecimal;

public interface FeeCalculationService {
    BigDecimal calculateFee(String feeCode, BigDecimal transactionAmount, Long currencyId);
}
