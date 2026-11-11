package io.github.hossensyedriadh.cbs.statement.internal.service;

import io.github.hossensyedriadh.cbs.statement.api.enumerator.StatementType;
import io.github.hossensyedriadh.cbs.statement.internal.domain.Statement;

import java.time.LocalDate;

public interface StatementGenerationService {
    Statement generate(Long accountId, LocalDate start, LocalDate end, StatementType type);
}
