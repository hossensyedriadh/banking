package io.github.hossensyedriadh.cbs.product.internal.domain;

import io.github.hossensyedriadh.cbs.product.api.enumerator.ChequeProductStatus;
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
@Table(name = "cheque_products", schema = "product", indexes = {@Index(name = "idx_cheque_products_status",
        columnList = "status")}, uniqueConstraints = {@UniqueConstraint(name = "uk_cheque_products_product_code",
        columnNames = {"product_code"})})
public class ChequeProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "product_code", updatable = false, nullable = false, length = 50)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "leaf_count", nullable = false)
    private Integer leafCount;

    @Column(name = "validity_days", nullable = false)
    private Integer validityDays;

    @ColumnDefault("0.0")
    @Column(name = "issuance_fee", nullable = false, precision = 19, scale = 4)
    private BigDecimal issuanceFee;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false, length = 20)
    private ChequeProductStatus status;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;
}