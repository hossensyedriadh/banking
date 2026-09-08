package io.github.hossensyedriadh.cbs.transaction.internal.util;

import io.github.hossensyedriadh.cbs.transaction.api.enumerator.TransactionType;

import java.util.UUID;

public final class TransactionReferenceGenerator {
    public static String generate(TransactionType type) {
        return type.getPrefix().concat("-").concat(UUID.randomUUID().toString());
    }
}
