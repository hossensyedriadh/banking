package io.github.hossensyedriadh.cbs.product.api.model;

import io.github.hossensyedriadh.cbs.product.api.enumerator.InterestCalculationMethod;
import jakarta.validation.constraints.PositiveOrZero;
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

    private String name;

    private String description;

    private Boolean interestBearing;

    @PositiveOrZero
    private BigDecimal interestRate;

    private InterestCalculationMethod interestCalculationMethod;
}
