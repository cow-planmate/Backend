package com.example.planmate.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Global async configuration for executing external API & IO bound tasks.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "placeExecutor")
    public Executor placeExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Core & max can be adjusted based on expected concurrency & API quota
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("place-async-");
        executor.setAwaitTerminationSeconds(10);
        executor.initialize();
        return executor;
    }
}
