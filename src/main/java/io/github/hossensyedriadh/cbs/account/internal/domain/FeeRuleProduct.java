package io.github.hossensyedriadh.cbs.account.internal.domain;

import io.github.hossensyedriadh.cbs.account.api.enumerator.FeeRuleProductType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Setter
@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "fee_rule_products", schema = "account", indexes = {@Index(name = "idx_fee_rule_products_product",
        columnList = "product_type, product_id")}, uniqueConstraints = {@UniqueConstraint(name = "uq_fee_rule_products",
        columnNames = {
                "fee_rule_id",
                "product_type",
                "product_id"})})
public class FeeRuleProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_rule_id", nullable = false)
    private FeeRule feeRule;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 20)
    private FeeRuleProductType productType;

    @Column(name = "product_id", nullable = false)
    private Long productId;
}