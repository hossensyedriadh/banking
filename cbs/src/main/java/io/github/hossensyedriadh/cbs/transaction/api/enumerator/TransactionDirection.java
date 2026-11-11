package io.github.hossensyedriadh.cbs.transaction.api.enumerator;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TransactionDirection {
    DEBIT ("DR"),
    CREDIT ("CR");

    private final String initials;
}
