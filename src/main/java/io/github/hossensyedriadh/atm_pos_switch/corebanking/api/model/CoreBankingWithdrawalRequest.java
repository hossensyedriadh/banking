package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class CoreBankingWithdrawalRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -3301918425102532456L;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("currency_id")
    private Long currencyId;

    private BigDecimal amount;

    @JsonProperty("idempotency_key")
    private String idempotencyKey;

    private String description;
}
