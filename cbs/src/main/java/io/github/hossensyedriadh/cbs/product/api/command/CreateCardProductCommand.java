package io.github.hossensyedriadh.cbs.product.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("product_code")
    private String productCode;

    @Size(max = 100)
    @NotBlank
    @JsonProperty("product_name")
    private String productName;

    @NotBlank
    private String description;

    @NotNull
    @JsonProperty("card_type")
    private CardType cardType;

    @NotNull
    @JsonProperty("card_network")
    private CardNetwork cardNetwork;

    @NotNull
    private CardProductStatus status;

    @NotNull
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
