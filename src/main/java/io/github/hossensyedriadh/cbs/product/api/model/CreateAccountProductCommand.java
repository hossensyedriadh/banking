package io.github.hossensyedriadh.cbs.product.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.product.api.enumerator.AccountProductType;
import io.github.hossensyedriadh.cbs.product.api.enumerator.InterestCalculationMethod;
import io.github.hossensyedriadh.cbs.product.api.validation.ValidInterestConfiguration;
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
@ValidInterestConfiguration
public class CreateAccountProductCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -8440033435156743717L;

    @NotBlank
    @JsonProperty("product_code")
    private String productCode;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    @JsonProperty("product_type")
    private AccountProductType productType;

    @NotNull
    private boolean interestBearing;

    @PositiveOrZero
    private BigDecimal interestRate;

    private InterestCalculationMethod interestCalculationMethod;
}
