package io.github.hossensyedriadh.cbs.statement.internal.repository;

import io.github.hossensyedriadh.cbs.statement.api.enumerator.StatementType;
import io.github.hossensyedriadh.cbs.statement.internal.domain.Statement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface StatementRepository extends JpaRepository<Statement, Long> {
    Page<Statement> findByAccountId(Long accountId, Pageable pageable);

    Optional<Statement> findByIdAndAccountId(Long id, Long accountId);

    Optional<Statement> findByAccountIdAndStatementPeriodStartAndStatementPeriodEndAndStatementType(Long accountId, LocalDate statementPeriodStart,
                                                                                                    LocalDate statementPeriodEnd, StatementType statementType);
}
