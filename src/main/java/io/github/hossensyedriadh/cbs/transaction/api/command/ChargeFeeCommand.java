package io.github.hossensyedriadh.cbs.transaction.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class ChargeFeeCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 2879455045067840994L;
    
    @NotNull
    @JsonProperty("account_id")
    private Long accountId;

    @NotNull
    @JsonProperty("currency_id")
    private Long currencyId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String feeCode;

    @NotBlank
    @JsonProperty("idempotency_key")
    private String idempotencyKey;

    private String description;
}
