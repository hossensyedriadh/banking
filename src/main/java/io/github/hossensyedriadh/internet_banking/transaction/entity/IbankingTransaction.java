package io.github.hossensyedriadh.internet_banking.transaction.entity;

import io.github.hossensyedriadh.internet_banking.transaction.enumerator.ChannelTransactionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "ibanking_transactions", schema = "core", indexes = {@Index(name = "uq_ibanking_transactions_reversal_original",
        columnList = "reversal_of_transaction_id", unique = true)},
        uniqueConstraints = {@UniqueConstraint(name = "uq_ibanking_transactions_idempotency",
                columnNames = {"cbs_account_id", "idempotency_key"})})
public class IbankingTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "idempotency_key", updatable = false, nullable = false)
    private UUID idempotencyKey;

    @Column(name = "request_hash", updatable = false, nullable = false)
    private byte[] requestHash;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'RECEIVED'")
    @Column(name = "status", nullable = false, length = 20)
    private ChannelTransactionStatus status;

    @Column(name = "request_payload", updatable = false, nullable = false)
    private byte[] requestPayload;

    @Column(name = "response_payload", insertable = false)
    private byte[] responsePayload;

    @Column(name = "cbs_transaction_id")
    private Long cbsTransactionId;

    @Column(name = "cbs_reversal_transaction_id")
    private Long cbsReversalTransactionId;

    @Column(name = "reversal_of_transaction_id")
    private Long reversalOfTransactionId;

    @Column(name = "cbs_account_id")
    private Long cbsAccountId;

    @Column(name = "cbs_currency_id")
    private Long cbsCurrencyId;

    @Column(name = "processing_started_at")
    private OffsetDateTime processingStartedAt;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "received_at", updatable = false, nullable = false)
    private OffsetDateTime receivedAt;

    @Column(name = "completed_at", insertable = false)
    private OffsetDateTime completedAt;
}