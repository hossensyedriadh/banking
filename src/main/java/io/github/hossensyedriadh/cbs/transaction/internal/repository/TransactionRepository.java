package io.github.hossensyedriadh.cbs.transaction.internal.repository;

import io.github.hossensyedriadh.cbs.transaction.internal.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionReference(String ref);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    boolean existsByReversalOfTransactionId(Long transactionId);

    @Query("""
            select t from Transaction t
            where t.accountId = :accountId
                or t.destinationAccountId = :accountId
            order by t.createdAt desc""")
    Page<Transaction> findAccountTransactions(@Param("accountId") Long accountId, Pageable pageable);
}
