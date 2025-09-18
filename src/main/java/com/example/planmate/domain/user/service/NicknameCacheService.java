package com.example.planmate.domain.user.service;

import static com.example.planmate.infrastructure.redis.RedisKeys.nicknameUser;
import static com.example.planmate.infrastructure.redis.RedisKeys.userNickname;

import org.springframework.data.redis.core.RedisTemplate;
// @Service annotation removed to prevent duplicate bean definition conflict with infrastructure.redis.NicknameCacheService.

import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
// Legacy duplicate class retained temporarily after refactor; NOT a Spring bean anymore.
class NicknameCacheService {

    private final RedisTemplate<String, String> userIdNicknameRedis; // key: USERIDNICKNAME+userId -> nickname
    private final RedisTemplate<String, Integer> nicknameUseridRedis; // key: NICKNAMEUSERID+nickname -> userId
    private final UserRepository userRepository;

    public void register(int userId, String nickname){
        userIdNicknameRedis.opsForValue().set(userNickname(userId), nickname);
        nicknameUseridRedis.opsForValue().set(nicknameUser(nickname), userId);
    }

    /** Load from DB and cache both directions. */
    public void register(int userId){
        User user = userRepository.findById(userId).orElseThrow();
        register(user.getUserId(), user.getNickname());
    }

    public String getNickname(int userId){
        return userIdNicknameRedis.opsForValue().get(userNickname(userId));
    }

    public Integer getUserId(String nickname){
        return nicknameUseridRedis.opsForValue().get(nicknameUser(nickname));
    }

    public void remove(int userId){
        String nickname = userIdNicknameRedis.opsForValue().getAndDelete(userNickname(userId));
        if(nickname != null){
            nicknameUseridRedis.delete(nicknameUser(nickname));
        }
    }
}
