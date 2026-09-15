package io.github.hossensyedriadh.cbs.product.internal.domain;

import io.github.hossensyedriadh.cbs.product.api.enumerator.AccountProductStatus;
import io.github.hossensyedriadh.cbs.product.api.enumerator.AccountProductType;
import io.github.hossensyedriadh.cbs.product.api.enumerator.InterestCalculationMethod;
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
@Table(name = "account_products", schema = "product")
public class AccountProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "product_code", updatable = false, unique = true, nullable = false, length = 30)
    private String productCode;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 30)
    private AccountProductType productType;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false)
    private AccountProductStatus status;

    @Column(name = "interest_bearing", nullable = false)
    private boolean interestBearing;

    @Column(name = "interest_rate", precision = 9, scale = 6)
    private BigDecimal interestRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_calculation_method", length = 30)
    private InterestCalculationMethod interestCalculationMethod;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;
}