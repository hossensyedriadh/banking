package io.github.hossensyedriadh.cbs.ledger.internal.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DistributedLockKeys {
    LEDGER_RECONCILE ("LEDGER_RECONCILE_JOB");

    private final String key;
}
