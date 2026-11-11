package io.github.hossensyedriadh.cbs.statement.api.event;

import java.time.YearMonth;

public record MonthlyStatementGeneratedEvent(Long statementId, YearMonth period) {
}
