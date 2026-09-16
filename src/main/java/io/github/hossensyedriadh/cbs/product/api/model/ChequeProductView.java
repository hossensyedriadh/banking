package io.github.hossensyedriadh.cbs.product.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.product.api.enumerator.ChequeProductStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class ChequeProductView implements Serializable {
    @Serial
    private static final long serialVersionUID = 5013991750586103148L;

    private Long id;

    @JsonProperty("product_code")
    private String productCode;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("leaf_count")
    private Integer leafCount;

    @JsonProperty("validity_days")
    private Integer validityDays;

    @JsonProperty("issuance_fee")
    private BigDecimal issuanceFee;

    private ChequeProductStatus status;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
