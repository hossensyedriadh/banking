package io.github.hossensyedriadh.cbs.eod.internal.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DistributedLockKeys {
    EOD_RUN ("EOD_RUN");

    private final String key;
}
