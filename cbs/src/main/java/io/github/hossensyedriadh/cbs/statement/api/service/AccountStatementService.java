package io.github.hossensyedriadh.cbs.statement.api.service;

import io.github.hossensyedriadh.cbs.statement.api.command.GenerateMonthlyStatementCommand;
import io.github.hossensyedriadh.cbs.statement.api.command.GenerateOnDemandStatementCommand;
import io.github.hossensyedriadh.cbs.statement.api.model.StatementView;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AccountStatementService {
    Page<StatementView> getStatements(Long accountId, Pageable pageable);

    Optional<StatementView> getStatement(Long accountId, Long statementId);

    StatementView generateMonthlyStatement(@Valid GenerateMonthlyStatementCommand command);

    StatementView generateOnDemandStatement(@Valid GenerateOnDemandStatementCommand command);
}
