package io.github.hossensyedriadh.cbs.ledger.internal.repository;

import io.github.hossensyedriadh.cbs.ledger.internal.domain.LedgerAccount;
import io.github.hossensyedriadh.cbs.ledger.internal.projection.SubLedgerReconciliationRow;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LedgerAccountRepository extends JpaRepository<LedgerAccount, Long> {
    Optional<LedgerAccount> findByAccountCode(String accountCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select 1 from LedgerAccount l where l.id = :id")
    Optional<LedgerAccount> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select new io.github.hossensyedriadh.cbs.ledger.internal.projection.SubLedgerReconciliationRow(
                        gl.id, gl.accountCode, gl.balance,
                        coalesce(sum(sl.balance), 0.0)
            ) from LedgerAccount gl left join SubLedgerAccount sl on sl.ledgerAccount.id = gl.id
            group by gl.id, gl.accountCode, gl.balance""")
    List<SubLedgerReconciliationRow> findSubLedgerReconciliationRows();
}
