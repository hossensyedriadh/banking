package io.github.hossensyedriadh.cbs.product.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.product.api.enumerator.InterestCalculationMethod;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class UpdateAccountProductCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -1825951808541646875L;

    @Size(max = 150)
    private String name;

    private String description;

    @JsonProperty("interest_bearing")
    private Boolean interestBearing;

    @PositiveOrZero
    @JsonProperty("interest_rate")
    private BigDecimal interestRate;

    @JsonProperty("interest_calculation_method")
    private InterestCalculationMethod interestCalculationMethod;
}
