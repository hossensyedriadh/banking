package io.github.hossensyedriadh.cbs.statement.internal.domain;

import io.github.hossensyedriadh.cbs.statement.api.enumerator.StatementStatus;
import io.github.hossensyedriadh.cbs.statement.api.enumerator.StatementType;
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
import java.util.List;

@Getter
@Setter
@DynamicInsert
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "statements", schema = "statement", indexes = {
        @Index(name = "uq_monthly_statement_account_period",
                columnList = "account_id, statement_period_start, statement_period_end",
                unique = true),
        @Index(name = "idx_statements_account_period",
                columnList = "account_id, statement_period_start, statement_period_end"),
        @Index(name = "idx_statements_account_id",
                columnList = "account_id"),
        @Index(name = "idx_statements_type",
                columnList = "statement_type"),
        @Index(name = "idx_statements_status",
                columnList = "status"),
        @Index(name = "idx_statements_generated_at",
                columnList = "generated_at")})
public class Statement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "account_id", updatable = false, nullable = false)
    private Long accountId;

    @Column(name = "currency_id", updatable = false, nullable = false)
    private Long currencyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "statement_type", updatable = false, nullable = false, length = 20)
    private StatementType statementType;

    @Column(name = "statement_period_start", updatable = false, nullable = false)
    private LocalDate statementPeriodStart;

    @Column(name = "statement_period_end", updatable = false, nullable = false)
    private LocalDate statementPeriodEnd;

    @Column(name = "opening_balance", updatable = false, nullable = false, precision = 19, scale = 4)
    private BigDecimal openingBalance;

    @Column(name = "closing_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal closingBalance;

    @ColumnDefault("0")
    @Column(name = "total_debits", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalDebits;

    @ColumnDefault("0")
    @Column(name = "total_credits", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalCredits;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatementStatus status;

    @OneToMany(mappedBy = "statement", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<StatementEntry> statementEntries;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "generated_at", updatable = false, nullable = false)
    private OffsetDateTime generatedAt;

    @Column(name = "issued_at")
    private OffsetDateTime issuedAt;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;
}