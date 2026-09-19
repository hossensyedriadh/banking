package io.github.hossensyedriadh.cbs.product.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class UpdateChequeProductCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 4430711034927650865L;

    @JsonProperty("product_name")
    private String productName;

    private String description;

    @Min(1)
    @JsonProperty("leaf_count")
    private Integer leafCount;

    @Min(1)
    @JsonProperty("validity_days")
    private Integer validityDays;

    @PositiveOrZero
    @JsonProperty("issuance_fee")
    private BigDecimal issuanceFee;
}
