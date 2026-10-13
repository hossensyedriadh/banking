package io.github.hossensyedriadh.cbs.account.internal.domain;

import io.github.hossensyedriadh.cbs.account.api.enumerator.ScheduledFeeStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@DynamicInsert
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "scheduled_fees", schema = "account", indexes = {
        @Index(name = "idx_scheduled_fees_account",
                columnList = "account_id, scheduled_date"),
        @Index(name = "idx_scheduled_fees_card",
                columnList = "card_id, scheduled_date"),
        @Index(name = "idx_scheduled_fees_due",
                columnList = "scheduled_date, id")})
public class ScheduledFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "fee_rule_id", updatable = false, nullable = false)
    private Long feeRuleId;

    @Column(name = "account_id", updatable = false, nullable = false)
    private Long accountId;

    @Column(name = "card_id", updatable = false)
    private Long cardId;

    @Column(name = "scheduled_date", updatable = false, nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "amount", updatable = false, nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'PENDING'")
    @Column(name = "status", nullable = false, length = 20)
    private ScheduledFeeStatus status;

    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "processing_started_at")
    private OffsetDateTime processingStartedAt;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;
}