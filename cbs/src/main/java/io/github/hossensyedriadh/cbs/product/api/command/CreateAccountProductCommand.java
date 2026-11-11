package io.github.hossensyedriadh.cbs.product.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.product.api.enumerator.AccountProductStatus;
import io.github.hossensyedriadh.cbs.product.api.enumerator.AccountProductType;
import io.github.hossensyedriadh.cbs.product.api.enumerator.InterestCalculationMethod;
import io.github.hossensyedriadh.cbs.product.api.validation.ValidInterestConfiguration;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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

    @Size(max = 50)
    @NotBlank
    @JsonProperty("product_code")
    private String productCode;

    @Size(max = 150)
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    @JsonProperty("product_type")
    private AccountProductType productType;

    @NotNull
    private AccountProductStatus status;

    @NotNull
    @JsonProperty("interest_bearing")
    private boolean interestBearing;

    @PositiveOrZero
    @JsonProperty("interest_rate")
    private BigDecimal interestRate;

    @JsonProperty("interest_calculation_method")
    private InterestCalculationMethod interestCalculationMethod;
}
