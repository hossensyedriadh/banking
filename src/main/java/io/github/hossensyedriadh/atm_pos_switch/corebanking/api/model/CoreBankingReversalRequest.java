package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CoreBankingReversalRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -8433174886010321805L;

    @NotNull
    @JsonProperty("transaction_id")
    private Long transactionId;

    @NotBlank
    @Size(min = 5, max = 100)
    @JsonProperty("idempotency_key")
    private String idempotencyKey;

    @NotBlank
    private String description;
}
