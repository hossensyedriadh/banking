package io.github.hossensyedriadh.cbs.transaction.internal.repository;

import io.github.hossensyedriadh.cbs.transaction.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.cbs.transaction.api.enumerator.TransactionType;
import io.github.hossensyedriadh.cbs.transaction.internal.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findAllByCreatedAtBetweenAndAccountIdAndStatusAndTransactionType(OffsetDateTime createdAtAfter, OffsetDateTime createdAtBefore, Long accountId,
                                                                                       TransactionStatus status, TransactionType transactionType, Pageable pageable);

    Page<Transaction> findAllByAccountId(Long accountId, Pageable pageable);

    @Query("""
            select t from Transaction t
                where (t.accountId = :accountId
                   or t.destinationAccountId = :accountId)
                and (t.status = 'COMPLETED' or t.status = 'REVERSED')
                and t.createdAt >= :from
                and t.createdAt < :to
            order by t.createdAt asc, t.id asc""")
    List<Transaction> findCompletedAccountTransactionsInDateRange(@Param("accountId") Long accountId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    Optional<Transaction> findByTransactionReference(String ref);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    boolean existsByReversalOfTransactionId(Long transactionId);
}
