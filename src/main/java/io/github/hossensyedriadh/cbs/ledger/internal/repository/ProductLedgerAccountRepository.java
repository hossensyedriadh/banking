package io.github.hossensyedriadh.cbs.ledger.internal.repository;

import io.github.hossensyedriadh.cbs.ledger.api.enumerator.ProductLedgerRole;
import io.github.hossensyedriadh.cbs.ledger.internal.domain.ProductLedgerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductLedgerAccountRepository extends JpaRepository<ProductLedgerAccount, Long> {
    @Query("select pla.ledgerAccount.id from ProductLedgerAccount pla where pla.productId = :productId and pla.ledgerRole = :ledgerRole")
    Optional<Long> findLedgerAccountIdByProductIdAndLedgerRole(@Param("productId") Long productId, @Param("ledgerRole") ProductLedgerRole ledgerRole);
}
