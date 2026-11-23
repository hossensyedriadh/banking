package io.github.hossensyedriadh.internet_banking.configuration.thread;

import io.github.hossensyedriadh.internet_banking.context.GatewayAuthenticationContext;
import io.github.hossensyedriadh.internet_banking.context.GatewayAuthenticationType;
import org.apache.logging.log4j.ThreadContext;
import org.jspecify.annotations.NonNull;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

public class MdcTaskDecorator implements TaskDecorator {

    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        Map<String, String> callerContext = ThreadContext.getImmutableContext();
        GatewayAuthenticationType authenticationType = GatewayAuthenticationContext.get();

        return () -> {
            Map<String, String> previousContext = ThreadContext.getImmutableContext();
            GatewayAuthenticationType previousAuthenticationType = GatewayAuthenticationContext.get();

            try {
                ThreadContext.clearMap();
                ThreadContext.putAll(callerContext);

                if (authenticationType != null) {
                    GatewayAuthenticationContext.set(authenticationType);
                } else {
                    GatewayAuthenticationContext.clear();
                }

                runnable.run();
            } finally {
                ThreadContext.clearMap();
                ThreadContext.putAll(previousContext);

                if (previousAuthenticationType != null) {
                    GatewayAuthenticationContext.set(previousAuthenticationType);
                } else {
                    GatewayAuthenticationContext.clear();
                }
            }
        };
    }
}
