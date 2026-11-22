package io.github.hossensyedriadh.internet_banking.util;

public final class CustomAttributes {
    private CustomAttributes() {
    }

    public static final String CORRELATION_ID = "X-Correlation-ID";
    public static final String REQUEST_ID = "X-Request-ID";
    public static final String TRANSACTION_IDEMPOTENCY_KEY_HEADER = "X-Transaction-Idempotency-Key";
}
