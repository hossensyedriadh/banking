package io.github.hossensyedriadh.cbs.statement.internal.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@DynamicInsert
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "statement_entries", schema = "statement", indexes = {
        @Index(name = "idx_statement_entries_statement_id",
                columnList = "statement_id"),
        @Index(name = "idx_statement_entries_transaction_id",
                columnList = "transaction_id"),
        @Index(name = "idx_statement_entries_transaction_date",
                columnList = "transaction_date")})
public class StatementEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "statement_id", nullable = false)
    private Statement statement;

    @Column(name = "transaction_id", updatable = false, nullable = false)
    private Long transactionId;

    @Column(name = "transaction_reference", updatable = false, nullable = false, length = 100)
    private String transactionReference;

    @Column(name = "transaction_date", updatable = false, nullable = false)
    private OffsetDateTime transactionDate;

    @Column(name = "value_date", updatable = false)
    private LocalDate valueDate;

    @Column(name = "description", updatable = false, length = 500)
    private String description;

    @ColumnDefault("0")
    @Column(name = "debit_amount", updatable = false, nullable = false, precision = 19, scale = 4)
    private BigDecimal debitAmount;

    @ColumnDefault("0")
    @Column(name = "credit_amount", updatable = false, nullable = false, precision = 19, scale = 4)
    private BigDecimal creditAmount;

    @Column(name = "running_balance", updatable = false, nullable = false, precision = 19, scale = 4)
    private BigDecimal runningBalance;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;
}