package com.example.planmate.domain.redis.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NicknameIndexService {
    private final RedisTemplate<String, String> userIdNicknameRedis;
    private final RedisTemplate<String, Integer> nicknameUseridRedis;

    private static final String USERID_NICKNAME_PREFIX = "USERIDNICKNAME";
    private static final String NICKNAME_USERID_PREFIX = "NICKNAMEUSERID";

    public String getNicknameByUserId(int userId) {
        return userIdNicknameRedis.opsForValue().get(USERID_NICKNAME_PREFIX + userId);
    }

    public Integer getUserIdByNickname(String nickname) {
        return nicknameUseridRedis.opsForValue().get(NICKNAME_USERID_PREFIX + nickname);
    }

    public void registerNickname(int userId, String nickname) {
        userIdNicknameRedis.opsForValue().set(USERID_NICKNAME_PREFIX + userId, nickname);
        nicknameUseridRedis.opsForValue().set(NICKNAME_USERID_PREFIX + nickname, userId);
    }

    public void removeNickname(int userId) {
        String nickname = userIdNicknameRedis.opsForValue().getAndDelete(USERID_NICKNAME_PREFIX + userId);
        if (nickname != null) {
            nicknameUseridRedis.delete(NICKNAME_USERID_PREFIX + nickname);
        }
    }
}
