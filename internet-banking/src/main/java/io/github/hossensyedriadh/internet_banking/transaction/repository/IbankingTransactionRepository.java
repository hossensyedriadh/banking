package io.github.hossensyedriadh.internet_banking.transaction.repository;

import io.github.hossensyedriadh.internet_banking.transaction.entity.IbankingTransaction;
import io.github.hossensyedriadh.internet_banking.transaction.enumerator.ChannelTransactionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IbankingTransactionRepository extends JpaRepository<IbankingTransaction, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from IbankingTransaction t where t.id = :id")
    Optional<IbankingTransaction> findByIdForUpdate(@Param("id") Long id);

    Optional<IbankingTransaction> findByIdempotencyKeyAndCbsAccountId(UUID idempotencyKey, Long cbsAccountId);

    boolean existsByIdempotencyKeyAndCbsAccountId(UUID idempotencyKey, Long cbsAccountId);

    @Modifying
    @Query(value = """
            INSERT INTO ibanking_transactions (
                cbs_account_id,
                idempotency_key,
                request_hash,
                request_payload,
                status
            )
            VALUES (
                :accountId,
                :idempotencyKey,
                :requestHash,
                :requestPayload,
                'RECEIVED'
            )
            ON CONFLICT (cbs_account_id, idempotency_key)
            DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("accountId") Long accountId, @Param("idempotencyKey") UUID idempotencyKey,
                       @Param("requestHash") byte[] requestHash, @Param("requestPayload") byte[] requestPayload);

    @Modifying
    @Query("""
            update IbankingTransaction t
            set t.status = :processing,
                t.processingStartedAt = :startedAt
            where t.id = :transactionId
              and t.status = :received
            """)
    int claimForProcessing(@Param("transactionId") Long transactionId, @Param("received") ChannelTransactionStatus received,
                           @Param("processing") ChannelTransactionStatus processing, @Param("startedAt") OffsetDateTime startedAt);

    @Modifying
    @Query("""
            update IbankingTransaction t
            set t.status = :processing,
                t.processingStartedAt = :startedAt
            where t.id = :transactionId
              and t.status = :unknown
            """)
    int claimForReconciliation(@Param("transactionId") Long transactionId, @Param("unknown") ChannelTransactionStatus unknown,
                               @Param("processing") ChannelTransactionStatus processing, @Param("startedAt") OffsetDateTime startedAt);

    Optional<IbankingTransaction> findByCbsAccountIdAndIdempotencyKey(Long accountId, UUID idempotencyKey);

    @Modifying
    @Query("""
            update IbankingTransaction t
            set t.status = :unknown,
                t.processingStartedAt = null
            where t.id = :transactionId
              and t.status = :processing
            """)
    int markTransactionUnknown(@Param("transactionId") Long transactionId, @Param("processing") ChannelTransactionStatus processing, @Param("unknown") ChannelTransactionStatus unknown);

    @Query("""
            select t.id
            from IbankingTransaction t
            where t.status = :status
            order by t.receivedAt asc
            """)
    List<Long> findUnknownTransactionIds(@Param("status") ChannelTransactionStatus status, Pageable pageable);

    @Modifying
    @Query("""
            update IbankingTransaction t
            set t.status = :failed,
                t.processingStartedAt = null
            where t.id = :transactionId
              and t.status = :processing
            """)
    int markTransactionFailed(@Param("transactionId") Long transactionId, @Param("processing") ChannelTransactionStatus processing, @Param("failed") ChannelTransactionStatus failed);
}