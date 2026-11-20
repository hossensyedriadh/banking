package io.github.hossensyedriadh.internet_banking.configuration.thread;

import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import java.util.Arrays;
import java.util.concurrent.Executor;

@Log4j2
@Configuration
public class AsyncConfiguration implements AsyncConfigurer {
    @Bean("iBankingTaskExecutor")
    public TaskExecutor taskExecutor(MdcTaskDecorator mdcTaskDecorator) {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("ibanking-async-");
        executor.setVirtualThreads(true);
        executor.setConcurrencyLimit(100);
        executor.setTaskDecorator(mdcTaskDecorator);

        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }

    @Override
    public Executor getAsyncExecutor() {
        return this.taskExecutor(this.mdcTaskDecorator());
    }

    @Override
    public @Nullable AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> log.error("Async method execution failed: Method: {}, Params: {}",
                method.toGenericString(), Arrays.toString(params), ex);
    }

    @Bean
    public MdcTaskDecorator mdcTaskDecorator() {
        return new MdcTaskDecorator();
    }
}
