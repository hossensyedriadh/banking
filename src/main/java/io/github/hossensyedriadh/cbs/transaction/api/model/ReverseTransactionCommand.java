package io.github.hossensyedriadh.cbs.transaction.api.model;

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

    @Size(min = 5, max = 100)
    @NotBlank
    @JsonProperty("idempotency_key")
    private String idempotencyKey;

    @NotBlank
    private String description;
}
