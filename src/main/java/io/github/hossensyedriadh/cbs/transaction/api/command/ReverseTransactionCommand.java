package io.github.hossensyedriadh.cbs.transaction.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class ReverseTransactionCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 4604360820216282864L;

    @NotNull
    @JsonProperty("transaction_id")
    private Long transactionId;

    @NotBlank
    @Size(min = 5, max = 100)
    @JsonProperty("idempotency_key")
    private String idempotencyKey;

    @NotBlank
    @Size(max = 255)
    private String description;
}
