package com.example.planmate.infrastructure.redis;

import static com.example.planmate.infrastructure.redis.RedisKeys.nicknameUser;
import static com.example.planmate.infrastructure.redis.RedisKeys.userNickname;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NicknameCacheService {
    private final RedisTemplate<String, String> userIdNicknameRedis;
    private final RedisTemplate<String, Integer> nicknameUseridRedis;
    private final UserRepository userRepository;
    public void register(int userId, String nickname){ userIdNicknameRedis.opsForValue().set(userNickname(userId), nickname); nicknameUseridRedis.opsForValue().set(nicknameUser(nickname), userId); }
    public void register(int userId){ User user = userRepository.findById(userId).orElseThrow(); register(user.getUserId(), user.getNickname()); }
    public String getNickname(int userId){ return userIdNicknameRedis.opsForValue().get(userNickname(userId)); }
    public Integer getUserId(String nickname){ return nicknameUseridRedis.opsForValue().get(nicknameUser(nickname)); }
    public void remove(int userId){ String nickname = userIdNicknameRedis.opsForValue().getAndDelete(userNickname(userId)); if(nickname!=null){ nicknameUseridRedis.delete(nicknameUser(nickname)); } }
}
