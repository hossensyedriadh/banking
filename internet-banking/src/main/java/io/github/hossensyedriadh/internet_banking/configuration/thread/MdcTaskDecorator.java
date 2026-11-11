package io.github.hossensyedriadh.internet_banking.configuration.thread;

import org.apache.logging.log4j.ThreadContext;
import org.jspecify.annotations.NonNull;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

public class MdcTaskDecorator implements TaskDecorator {
    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        Map<String, String> callerContext = ThreadContext.getImmutableContext();

        return () -> {
            Map<String, String> previousContext = ThreadContext.getImmutableContext();

            try {
                ThreadContext.clearMap();
                ThreadContext.putAll(callerContext);
                runnable.run();
            } finally {
                ThreadContext.clearMap();
                ThreadContext.putAll(previousContext);
            }
        };
    }
}
