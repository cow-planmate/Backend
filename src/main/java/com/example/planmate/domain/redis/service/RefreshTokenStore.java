package com.example.planmate.domain.redis.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenStore {
    private final RedisTemplate<String, Integer> refreshTokenRedis;
    private static final String REFRESHTOKEN_PREFIX = "REFRESHTOKEN";

    public void registerRefreshToken(String token, int userId) {
        long ttl = 14L; // 14일
        refreshTokenRedis.opsForValue().set(
                REFRESHTOKEN_PREFIX + token,
                userId,
                ttl,
                TimeUnit.DAYS
        );
    }

    public Integer findUserIdByRefreshToken(String refreshToken) {
        return refreshTokenRedis.opsForValue().get(REFRESHTOKEN_PREFIX + refreshToken);
    }

    public void deleteRefreshToken(String refreshToken) {
        refreshTokenRedis.delete(REFRESHTOKEN_PREFIX + refreshToken);
    }
}
