package io.github.hossensyedriadh.cbs.statement.api.event;

import java.time.LocalDate;

public record OnDemandStatementGeneratedEvent(Long statementId, LocalDate from, LocalDate to) {
}
