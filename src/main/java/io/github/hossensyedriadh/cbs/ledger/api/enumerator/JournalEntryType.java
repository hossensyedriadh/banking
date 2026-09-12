package io.github.hossensyedriadh.cbs.ledger.api.enumerator;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum JournalEntryType {
    DEBIT("DR"),
    CREDIT("CR");

    private final String type;
}
