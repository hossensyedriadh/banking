package io.github.hossensyedriadh.cbs.product.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class UpdateCardProductCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 6640811579105651810L;

    @JsonProperty("product_name")
    private String productName;

    private String description;

    @Min(1)
    @Max(5)
    @JsonProperty("validity_years")
    private Integer validityYears;

    @PositiveOrZero
    @JsonProperty("issuance_fee")
    private BigDecimal issuanceFee;

    @PositiveOrZero
    @JsonProperty("annual_fee")
    private BigDecimal annualFee;

    @PositiveOrZero
    @JsonProperty("replacement_fee")
    private BigDecimal replacementFee;
}
