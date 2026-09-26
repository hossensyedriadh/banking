package io.github.hossensyedriadh.cbs.cheque.internal.domain;

import io.github.hossensyedriadh.cbs.cheque.api.enumerator.ChequePresentationStatus;
import io.github.hossensyedriadh.cbs.cheque.api.enumerator.ChequePresentationType;
import io.github.hossensyedriadh.cbs.cheque.api.enumerator.ChequeReturnReason;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@DynamicInsert
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "cheque_presentations", schema = "cheque", indexes = {
        @Index(name = "idx_cheque_presentations_cheque_id",
                columnList = "cheque_id"),
        @Index(name = "idx_cheque_presentations_status",
                columnList = "status"),
        @Index(name = "idx_cheque_presentations_presented_at",
                columnList = "presented_at")})
public class ChequePresentation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cheque_id", nullable = false)
    private Cheque cheque;

    @Enumerated(EnumType.STRING)
    @Column(name = "presentation_type", updatable = false, nullable = false)
    private ChequePresentationType presentationType;

    @Column(name = "presenting_bank_code", updatable = false, length = 30)
    private String presentingBankCode;

    @Column(name = "presenting_account_no", updatable = false, length = 100)
    private String presentingAccountNo;

    @Column(name = "presented_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal presentedAmount;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'PENDING'")
    @Column(name = "status", nullable = false, length = 20)
    private ChequePresentationStatus status;

    @Column(name = "presented_at", nullable = false)
    private OffsetDateTime presentedAt;

    @Column(name = "accepted_at")
    private OffsetDateTime acceptedAt;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @Column(name = "returned_at")
    private OffsetDateTime returnedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_reason", length = 30)
    private ChequeReturnReason returnReason;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @LastModifiedDate
    @Column(name = "updated_at", insertable = false)
    private OffsetDateTime updatedAt;
}