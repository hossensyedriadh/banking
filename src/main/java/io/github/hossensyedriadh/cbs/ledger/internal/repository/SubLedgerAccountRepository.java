package io.github.hossensyedriadh.cbs.ledger.internal.repository;

import io.github.hossensyedriadh.cbs.ledger.internal.domain.SubLedgerAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface SubLedgerAccountRepository extends JpaRepository<SubLedgerAccount, Long> {
    Optional<Long> findLedgerAccountIdByAccountId(Long accountId);

    Optional<Long> findSubLedgerAccountIdByAccountId(Long accountId);

    Optional<SubLedgerAccount> findSubLedgerAccountByAccountId(Long accountId);

    boolean existsByAccountId(Long accountId);

    boolean existsByLedgerAccountId(Long ledgerAccountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select sl from SubLedgerAccount sl where sl.id = :id")
    Optional<SubLedgerAccount> findByIdForUpdate(@Param("id") Long id);

    @Query("select coalesce(sum(s.balance), 0.0) from SubLedgerAccount s where s.ledgerAccount.id = :ledgerAccountId")
    BigDecimal sumBalanceByLedgerAccountId(@Param("ledgerAccountId") Long ledgerAccountId);
}
