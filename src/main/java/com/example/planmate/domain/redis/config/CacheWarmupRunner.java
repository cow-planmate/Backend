package com.example.planmate.domain.redis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.example.planmate.domain.redis.cache.PlaceCategoryCacheService;
import com.example.planmate.domain.redis.cache.TravelCacheService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheWarmupRunner implements ApplicationRunner {

    private final TravelCacheService travelCacheService;
    private final PlaceCategoryCacheService placeCategoryCacheService;

    @Value("${app.redis.warmup:false}")
    private boolean enableWarmup;

    @Override
    public void run(ApplicationArguments args) {
        if (!enableWarmup) {
            log.info("[CacheWarmup] Skipped (app.redis.warmup=false)");
            return;
        }
        try {
            travelCacheService.warmupAll();
            placeCategoryCacheService.warmupAll();
            log.info("[CacheWarmup] Completed");
        } catch (Exception e) {
            log.error("[CacheWarmup] Failed but continuing. cause={}", e.getMessage());
        }
    }
}
