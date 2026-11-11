package io.github.hossensyedriadh.cbs.ledger.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class TrialBalanceResult implements Serializable {
    @Serial
    private static final long serialVersionUID = 1063532555806031584L;

    @JsonProperty("total_debits")
    private BigDecimal totalDebits;

    @JsonProperty("total_credits")
    private BigDecimal totalCredits;

    @JsonProperty("difference")
    private BigDecimal difference;

    public boolean isBalanced() {
        return difference.compareTo(BigDecimal.ZERO) == 0;
    }
}
