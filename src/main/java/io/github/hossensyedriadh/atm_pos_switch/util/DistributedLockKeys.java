package io.github.hossensyedriadh.atm_pos_switch.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DistributedLockKeys {
    RECOVER_STUCK_TRANSACTIONS ("switch:RECOVER_STUCK_TRANSACTIONS"),
    RECONCILE_UNKNOWN_TRANSACTIONS ("switch:RECONCILE_UNKNOWN_TRANSACTIONS");

    private final String key;
}
