package com.example.planmate.common.oauth.service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthCodeService {

    private static final long LOGIN_CODE_TTL = 30; // seconds

    private final StringRedisTemplate redisTemplate;

    /**
     * 로그인용 1회용 code 발급
     */
    public String issueLoginCode(UUID userId) {
        String code = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(
                loginKey(code),
                userId.toString(),
                LOGIN_CODE_TTL,
                TimeUnit.SECONDS
        );

        return code;
    }

    /**
     * loginCode 검증 + 소비 (1회용)
     */
    public UUID consumeLoginCode(String code) {
        String key = loginKey(code);

        String userIdStr = redisTemplate.opsForValue().get(key);
        if (userIdStr == null) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 로그인 코드입니다");
        }

        // 🔥 1회용 보장
        redisTemplate.delete(key);

        return UUID.fromString(userIdStr);
    }

    /**
     * Redis key 네이밍 규칙
     */
    private String loginKey(String code) {
        return "oauth:login:" + code;
    }
}
