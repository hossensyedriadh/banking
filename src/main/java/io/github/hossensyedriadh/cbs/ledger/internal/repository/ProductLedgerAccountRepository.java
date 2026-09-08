package io.github.hossensyedriadh.cbs.ledger.internal.repository;

import io.github.hossensyedriadh.cbs.ledger.internal.domain.ProductLedgerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductLedgerAccountRepository extends JpaRepository<ProductLedgerAccount, Long> {
    Optional<Long> findLedgerAccountIdByProductId(Long productId);
}
