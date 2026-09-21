package io.github.hossensyedriadh.cbs.product.api.command;

import io.github.hossensyedriadh.cbs.product.api.enumerator.CardNetwork;
import io.github.hossensyedriadh.cbs.product.api.enumerator.CardProductStatus;
import io.github.hossensyedriadh.cbs.product.api.enumerator.CardType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class CreateCardProductCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 386049108088792519L;

    @Size(max = 50)
    @NotBlank
    private String productCode;

    @Size(max = 100)
    @NotBlank
    private String productName;

    @NotBlank
    private String description;

    @NotNull
    private CardType cardType;

    @NotNull
    private CardNetwork cardNetwork;

    @NotNull
    private CardProductStatus status;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer validityYears;

    @PositiveOrZero
    private BigDecimal issuanceFee;

    @PositiveOrZero
    private BigDecimal annualFee;

    @PositiveOrZero
    private BigDecimal replacementFee;
}
