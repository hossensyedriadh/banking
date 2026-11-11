package io.github.hossensyedriadh.cbs.eod.api.enumerator;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EodStep {
    INTEREST_ACCRUAL(1),
    HOLD_EXPIRATION(2),
    LEDGER_RECONCILIATION(3),
    TRIAL_BALANCE(4),
    CLOSE_BUSINESS_DATE(5);

    private final int order;
}
