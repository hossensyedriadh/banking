package io.github.hossensyedriadh.cbs.account.internal.domain;

import io.github.hossensyedriadh.cbs.account.api.enumerator.InterestAccrualStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
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
@Table(name = "interest_accruals", schema = "account", indexes = {
        @Index(name = "uk_interest_accrual_account_date",
                columnList = "account_id,calculation_date",
                unique = true)
})
public class InterestAccrual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "account_id", updatable = false, nullable = false)
    private Long accountId;

    @Column(name = "calculation_date", updatable = false, nullable = false)
    private LocalDate calculationDate;

    @Column(name = "base_amount", updatable = false, nullable = false, precision = 19, scale = 4)
    private BigDecimal baseAmount;

    @Column(name = "interest_rate", updatable = false, nullable = false, precision = 9, scale = 6)
    private BigDecimal interestRate;

    @Column(name = "interest_amount", updatable = false, nullable = false, precision = 19, scale = 4)
    private BigDecimal interestAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InterestAccrualStatus status;

    @Column(name = "posted_at")
    private OffsetDateTime postedAt;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;
}