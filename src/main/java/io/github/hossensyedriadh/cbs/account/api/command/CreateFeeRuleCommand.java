package io.github.hossensyedriadh.cbs.account.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.account.api.enumerator.FeeType;
import io.github.hossensyedriadh.cbs.account.api.validation.ValidFeeRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@ValidFeeRule
public class CreateFeeRuleCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 7095049503135381072L;

    @NotBlank
    @Size(max = 50)
    @JsonProperty("fee_code")
    private String feeCode;

    @NotBlank
    @Size(max = 100)
    @JsonProperty("fee_name")
    private String feeName;

    @NotNull
    @JsonProperty("fee_type")
    private FeeType feeType;

    @Positive
    @JsonProperty("fixed_amount")
    private BigDecimal fixedAmount;

    @Positive
    private BigDecimal percentage;

    @JsonProperty("currency_id")
    private Long currencyId;
}
