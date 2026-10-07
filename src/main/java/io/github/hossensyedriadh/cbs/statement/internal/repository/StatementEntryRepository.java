package io.github.hossensyedriadh.cbs.statement.internal.repository;

import io.github.hossensyedriadh.cbs.statement.internal.domain.StatementEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatementEntryRepository extends JpaRepository<StatementEntry, Long> {
}
