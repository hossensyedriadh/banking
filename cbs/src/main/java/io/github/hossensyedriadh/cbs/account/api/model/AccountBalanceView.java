package io.github.hossensyedriadh.cbs.account.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class AccountBalanceView implements Serializable {
    @Serial
    private static final long serialVersionUID = -951109485119573230L;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("ledger_balance")
    private BigDecimal ledgerBalance;

    @JsonProperty("held_amount")
    private BigDecimal heldAmount;

    @JsonProperty("available_balance")
    private BigDecimal availableBalance;
}
