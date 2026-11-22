package io.github.hossensyedriadh.internet_banking.context;

public final class GatewayAuthenticationContext {
    private static final ThreadLocal<GatewayAuthenticationType> CONTEXT = new ThreadLocal<>();

    private GatewayAuthenticationContext() {}

    public static GatewayAuthenticationType get() {
        return CONTEXT.get();
    }

    public static void set(GatewayAuthenticationType type) {
        CONTEXT.set(type);
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
