package io.github.hossensyedriadh.cbs.transaction.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class CreateTransferCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -6423478944093402978L;

    @NotNull
    @JsonProperty("account_id")
    private Long accountId;

    @NotNull
    @JsonProperty("destination_account_id")
    private Long destinationAccountId;

    @NotNull
    @JsonProperty("currency_id")
    private Long currencyId;

    @NotNull
    @Positive
    private BigDecimal amount;

    private String description;
}
