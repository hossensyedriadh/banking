package io.github.hossensyedriadh.cbs.product.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.product.api.enumerator.ChequeProductStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class CreateChequeProductCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 6250175179778988855L;

    @Size(max = 50)
    @NotBlank
    @JsonProperty("product_code")
    private String productCode;

    @Size(max = 100)
    @NotBlank
    @JsonProperty("product_name")
    private String productName;

    @NotBlank
    private String description;

    @NotNull
    @Min(1)
    @JsonProperty("leaf_count")
    private Integer leafCount;

    @NotNull
    @Min(1)
    @JsonProperty("validity_days")
    private Integer validityDays;

    @PositiveOrZero
    @JsonProperty("issuance_fee")
    private BigDecimal issuanceFee;

    @NotNull
    private ChequeProductStatus status;
}
