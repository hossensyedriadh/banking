package io.github.hossensyedriadh.cbs.eod.api.enumerator;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EodStep {
    INTEREST_ACCRUAL(1),
    HOLD_EXPIRATION(2),
    SCHEDULED_FEES(3),
    LEDGER_RECONCILIATION(4),
    TRIAL_BALANCE(5),
    CLOSE_BUSINESS_DATE(6);

    private final int order;
}
