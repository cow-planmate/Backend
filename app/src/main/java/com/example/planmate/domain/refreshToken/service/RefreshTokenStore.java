package com.example.planmate.domain.refreshToken.service;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenStore {

    private final RedisTemplate<String, Integer> refreshTokenRedis;


    private String key(Object suffix) {
        return "REFRESHTOKEN" + suffix;
    }

    public void insertRefreshToken(String token, int userId) {
        long ttl = 14L; // 14일
    refreshTokenRedis.opsForValue().set(key(token), userId, ttl, TimeUnit.DAYS);
    }

    public Integer findUserIdByRefreshToken(String refreshToken) {
        return refreshTokenRedis.opsForValue().get(key(refreshToken));
    }

    public void deleteRefreshToken(String refreshToken) {
        refreshTokenRedis.delete(key(refreshToken));
    }
}
