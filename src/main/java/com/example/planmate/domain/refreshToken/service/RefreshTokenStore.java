package com.example.planmate.domain.refreshToken.service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenStore {

    private final RedisTemplate<String, UUID> refreshTokenRedis;

    public RefreshTokenStore(
            @Qualifier("refreshTokenRedis")
            RedisTemplate<String, UUID> refreshTokenRedis
    ) {
        this.refreshTokenRedis = refreshTokenRedis;
    }

    private String key(Object suffix) {
        return "REFRESHTOKEN:" + suffix;
    }

    public void insertRefreshToken(String token, UUID userId) {
        refreshTokenRedis.opsForValue()
                .set(key(token), userId, 14L, TimeUnit.DAYS);
    }

    public UUID findUserIdByRefreshToken(String refreshToken) {
        return refreshTokenRedis.opsForValue().get(key(refreshToken));
    }

    public void deleteRefreshToken(String refreshToken) {
        refreshTokenRedis.delete(key(refreshToken));
    }
}
