package io.github.hossensyedriadh.cbs.cheque.internal.domain;

import io.github.hossensyedriadh.cbs.cheque.api.enumerator.ChequeStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@DynamicInsert
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "cheques", schema = "cheque", indexes = {
        @Index(name = "idx_cheques_cheque_book_id",
                columnList = "cheque_book_id"),
        @Index(name = "idx_cheques_status",
                columnList = "status"),
        @Index(name = "idx_cheques_expiry_date",
                columnList = "expiry_date")}, uniqueConstraints = {@UniqueConstraint(name = "uk_cheques_cheque_number",
        columnNames = {"cheque_number"})})
public class Cheque {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cheque_book_id", nullable = false)
    private ChequeBook chequeBook;

    @Column(name = "cheque_number", updatable = false, nullable = false)
    private Long chequeNumber;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'AVAILABLE'")
    @Column(name = "status", nullable = false, length = 20)
    private ChequeStatus status;

    @Column(name = "issued_at", updatable = false, nullable = false)
    private OffsetDateTime issuedAt;

    @Column(name = "expiry_date", updatable = false, nullable = false)
    private LocalDate expiryDate;

    @Column(name = "presented_at")
    private OffsetDateTime presentedAt;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @Column(name = "bounced_at")
    private OffsetDateTime bouncedAt;

    @Column(name = "stopped_at")
    private OffsetDateTime stoppedAt;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;
}