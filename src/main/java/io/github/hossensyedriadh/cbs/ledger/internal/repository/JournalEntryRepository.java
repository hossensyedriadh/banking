package io.github.hossensyedriadh.cbs.ledger.internal.repository;

import io.github.hossensyedriadh.cbs.ledger.internal.domain.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    @Query("""
            SELECT COALESCE(
                SUM(
                    CASE
                        WHEN j.entryType = 'CREDIT' THEN j.amount
                        WHEN j.entryType = 'DEBIT' THEN -j.amount
                        ELSE 0
                    END
                ),
                0
            )
            FROM JournalEntry j
            WHERE j.subLedgerAccount.id = :subLedgerAccountId
              AND j.createdAt < :asOf""")
    BigDecimal getBalanceBefore(@Param("subLedgerAccountId") Long subLedgerAccountId, @Param("asOf") OffsetDateTime asOf);
}
