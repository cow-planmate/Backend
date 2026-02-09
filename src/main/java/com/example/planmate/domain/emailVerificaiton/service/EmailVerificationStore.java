package com.example.planmate.domain.emailVerificaiton.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.planmate.domain.emailVerificaiton.enums.EmailVerificationPurpose;

@Service
public class EmailVerificationStore {

    private final RedisTemplate<String, String> emailVerificationRedis;

    public EmailVerificationStore(
            @Qualifier("emailVerificationRedis")
            RedisTemplate<String, String> emailVerificationRedis
    ) {
        this.emailVerificationRedis = emailVerificationRedis;
    }

    private String key(String email, EmailVerificationPurpose purpose) {
        return "EMAIL_VERIF:" + purpose.name() + ":" + email;
    }

    public void saveCode(String email, EmailVerificationPurpose purpose, String code) {
        emailVerificationRedis.opsForValue()
                .set(key(email, purpose), code, 5L, TimeUnit.MINUTES);
    }

    public String getCode(String email, EmailVerificationPurpose purpose) {
        return emailVerificationRedis.opsForValue().get(key(email, purpose));
    }

    public void deleteCode(String email, EmailVerificationPurpose purpose) {
        emailVerificationRedis.delete(key(email, purpose));
    }
}
