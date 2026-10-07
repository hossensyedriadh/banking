package io.github.hossensyedriadh.cbs.statement.internal.repository;

import io.github.hossensyedriadh.cbs.statement.internal.domain.Statement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatementRepository extends JpaRepository<Statement, Long> {
}
