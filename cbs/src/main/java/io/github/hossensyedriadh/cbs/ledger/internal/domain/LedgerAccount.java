package io.github.hossensyedriadh.cbs.ledger.internal.domain;

import io.github.hossensyedriadh.cbs.ledger.api.enumerator.LedgerAccountType;
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
import java.time.OffsetDateTime;

@Getter
@Setter
@DynamicInsert
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "ledger_accounts", schema = "ledger")
public class LedgerAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "account_code", unique = true, updatable = false, nullable = false, length = 30)
    private String accountCode;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "currency_id", updatable = false, nullable = false)
    private Long currencyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", updatable = false, nullable = false, length = 30)
    private LedgerAccountType accountType;

    @ColumnDefault("true")
    @Column(name = "active", nullable = false)
    private Boolean active;

    @ColumnDefault("0.0")
    @Column(name = "balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;
}