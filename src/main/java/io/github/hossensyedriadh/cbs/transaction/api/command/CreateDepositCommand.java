package io.github.hossensyedriadh.cbs.transaction.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class CreateDepositCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -7948189728480103728L;

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
    @Size(min = 5, max = 100)
    @JsonProperty("idempotency_key")
    private String idempotencyKey;

    @Size(max = 255)
    private String description;
}
