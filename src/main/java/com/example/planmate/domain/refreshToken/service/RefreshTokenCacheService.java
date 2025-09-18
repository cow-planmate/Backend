package com.example.planmate.domain.refreshToken.service;

import static com.example.planmate.infrastructure.redis.RedisKeys.REFRESH_TOKEN;
import static com.example.planmate.infrastructure.redis.RedisKeys.refreshToken;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
// @Service removed (infrastructure.redis.RefreshTokenCacheService is the active bean)

import lombok.RequiredArgsConstructor;

/**
 * Dedicated cache access for refresh tokens (token -> userId).
 */
@RequiredArgsConstructor
class RefreshTokenCacheService {

    private final RedisTemplate<String, Integer> refreshTokenRedis;

    private static final long DEFAULT_TTL_DAYS = 14L;

    /** Store with default TTL (14 days). */
    public void store(String token, int userId){
        store(token, userId, DEFAULT_TTL_DAYS);
    }

    /** Store with provided TTL in days. */
    public void store(String token, int userId, long ttlDays){
        refreshTokenRedis.opsForValue().set(refreshToken(token), userId, ttlDays, TimeUnit.DAYS);
    }

    public Integer findUserId(String token){
        return refreshTokenRedis.opsForValue().get(refreshToken(token));
    }

    public void delete(String token){
        refreshTokenRedis.delete(refreshToken(token));
    }

    // Backward compatibility accessor (if legacy prefix references needed elsewhere)
    public String getPrefix(){
        return REFRESH_TOKEN; // raw constant
    }
}
