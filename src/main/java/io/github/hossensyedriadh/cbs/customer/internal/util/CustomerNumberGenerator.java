package io.github.hossensyedriadh.cbs.customer.internal.util;

import java.util.UUID;

public final class CustomerNumberGenerator {
    public static String generate() {
        return "C" + UUID.randomUUID().toString().replace("-", "")
                .substring(0,19).toUpperCase();
    }
}
