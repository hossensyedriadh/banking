package io.github.hossensyedriadh.cbs.transaction.api.enumerator;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionType {
    DEPOSIT("D"),
    WITHDRAWAL("W"),
    TRANSFER("T");

    private final String prefix;
}
