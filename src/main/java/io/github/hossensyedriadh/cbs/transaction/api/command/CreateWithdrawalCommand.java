package io.github.hossensyedriadh.cbs.transaction.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateWithdrawalCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -5110116288505004076L;

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
    @JsonProperty("idempotency_key")
    private String idempotencyKey;

    private String description;
}
