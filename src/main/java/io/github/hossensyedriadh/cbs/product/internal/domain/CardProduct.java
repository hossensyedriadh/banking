package io.github.hossensyedriadh.cbs.product.internal.domain;

import io.github.hossensyedriadh.cbs.product.api.enumerator.CardNetwork;
import io.github.hossensyedriadh.cbs.product.api.enumerator.CardProductStatus;
import io.github.hossensyedriadh.cbs.product.api.enumerator.CardType;
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
@Table(name = "card_products", schema = "product")
public class CardProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "product_code", unique = true, updatable = false, nullable = false, length = 50)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", updatable = false, nullable = false, length = 20)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_network", updatable = false, nullable = false, length = 30)
    private CardNetwork cardNetwork;

    @Column(name = "validity_years", nullable = false)
    private Integer validityYears;

    @ColumnDefault("0.0")
    @Column(name = "issuance_fee", nullable = false, precision = 19, scale = 4)
    private BigDecimal issuanceFee;

    @ColumnDefault("0.0")
    @Column(name = "annual_fee", nullable = false, precision = 19, scale = 4)
    private BigDecimal annualFee;

    @ColumnDefault("0.0")
    @Column(name = "replacement_fee", nullable = false, precision = 19, scale = 4)
    private BigDecimal replacementFee;

    @ColumnDefault("'ACTIVE'")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CardProductStatus status;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;
}