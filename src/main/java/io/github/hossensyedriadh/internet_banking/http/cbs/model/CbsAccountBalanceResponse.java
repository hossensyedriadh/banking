package io.github.hossensyedriadh.internet_banking.http.cbs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class CbsAccountBalanceResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = -7607245598242018425L;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("ledger_balance")
    private BigDecimal ledgerBalance;

    @JsonProperty("available_balance")
    private BigDecimal availableBalance;
}
