package io.github.hossensyedriadh.cbs.ledger.internal.repository;

import io.github.hossensyedriadh.cbs.ledger.internal.domain.SubLedgerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubLedgerAccountRepository extends JpaRepository<SubLedgerAccount, Long> {
    Optional<SubLedgerAccount> findSubLedgerAccountByAccountId(Long accountId);

    boolean existsByAccountId(Long accountId);

    boolean existsByLedgerAccountId(Long ledgerAccountId);
}
