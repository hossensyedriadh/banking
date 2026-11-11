package io.github.hossensyedriadh.cbs.transaction.api.enumerator;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionType {
    DEPOSIT("D"),
    WITHDRAWAL("W"),
    TRANSFER("T"),
    REVERSAL ("R"),
    FEE ("F"),
    INTEREST ("I");

    private final String prefix;
}
