package io.github.hossensyedriadh.cbs.ledger.api.enumerator;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LedgerAccountType {
    ASSET(false),
    LIABILITY(true),
    EQUITY(true),
    INCOME(true),
    EXPENSE(false);

    private final boolean credit;
}
