package com.example.planmate.infrastructure.redis;

import static com.example.planmate.infrastructure.redis.RedisKeys.REFRESH_TOKEN;
import static com.example.planmate.infrastructure.redis.RedisKeys.refreshToken;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenCacheService {
    private final RedisTemplate<String, Integer> refreshTokenRedis;
    private static final long DEFAULT_TTL_DAYS = 14L;
    public void store(String token, int userId){ store(token, userId, DEFAULT_TTL_DAYS); }
    public void store(String token, int userId, long ttlDays){ refreshTokenRedis.opsForValue().set(refreshToken(token), userId, ttlDays, TimeUnit.DAYS); }
    public Integer findUserId(String token){ return refreshTokenRedis.opsForValue().get(refreshToken(token)); }
    public void delete(String token){ refreshTokenRedis.delete(refreshToken(token)); }
    public String getPrefix(){ return REFRESH_TOKEN; }
}
