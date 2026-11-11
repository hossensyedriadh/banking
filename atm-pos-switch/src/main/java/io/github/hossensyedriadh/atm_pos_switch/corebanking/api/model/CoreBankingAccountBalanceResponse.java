package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class CoreBankingAccountBalanceResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = -6075292313865717445L;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("ledger_balance")
    private BigDecimal ledgerBalance;

    @JsonProperty("available_balance")
    private BigDecimal availableBalance;
}
