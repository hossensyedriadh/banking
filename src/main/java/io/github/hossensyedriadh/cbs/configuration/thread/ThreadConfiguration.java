package io.github.hossensyedriadh.cbs.configuration.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ThreadConfiguration {
    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(15);
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(10);
    }

    @Bean
    public Executor virtualThreadExecutor() {
        return Executors.newSingleThreadExecutor(Thread.ofVirtual().factory());
    }
}
