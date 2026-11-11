package io.github.hossensyedriadh.cbs.statement.internal.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface StatementBalanceService {
    BigDecimal getOpeningBalance(Long accountId, LocalDate statementPeriodStart);
}
