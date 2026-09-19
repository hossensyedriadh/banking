package io.github.hossensyedriadh.cbs.product.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.product.api.enumerator.ChequeProductStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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

    @NotBlank
    @JsonProperty("product_code")
    private String productCode;

    @NotBlank
    @JsonProperty("product_name")
    private String productName;

    @NotBlank
    private String description;

    @NotNull
    @Min(1)
    @JsonProperty("validity_days")
    private Integer validityDays;

    @NotNull
    @PositiveOrZero
    @JsonProperty("issuance_fee")
    private BigDecimal issuanceFee;

    @NotNull
    private ChequeProductStatus status;
}
