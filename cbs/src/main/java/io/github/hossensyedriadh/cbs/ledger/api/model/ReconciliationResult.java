package io.github.hossensyedriadh.cbs.ledger.api.model;

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
public class ReconciliationResult implements Serializable {
    @Serial
    private static final long serialVersionUID = 7210225299521261560L;

    @JsonProperty("gl_account_id")
    private Long glAccountId;

    @JsonProperty("account_code")
    private String accountCode;

    @JsonProperty("gl_balance")
    private BigDecimal glBalance;

    @JsonProperty("sub_ledger_balance")
    private BigDecimal subLedgerBalance;

    private BigDecimal difference;

    private boolean reconciled;
}
