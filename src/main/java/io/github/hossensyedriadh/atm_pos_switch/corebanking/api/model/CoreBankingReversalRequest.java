package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("transaction_id")
    private Long transactionId;

    @JsonProperty("idempotency_key")
    private String idempotencyKey;

    private String description;
}
