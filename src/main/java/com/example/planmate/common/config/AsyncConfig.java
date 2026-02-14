package com.example.planmate.common.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Global async configuration for executing external API & IO bound tasks.
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    @PostConstruct
    public void init() {
        log.info("AsyncConfig initialized");
    }

    @Bean(name = "customPlaceExecutor")
    public Executor customPlaceExecutor() {
        log.info("Configuring customPlaceExecutor with core=20, max=50, queue=500");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Core & max can be adjusted based on expected concurrency & API quota
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("place-async-");
        executor.setAwaitTerminationSeconds(10);
        executor.initialize();
        return executor;
    }
}
