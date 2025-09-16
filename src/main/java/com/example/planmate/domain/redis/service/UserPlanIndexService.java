package com.example.planmate.domain.redis.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserPlanIndexService {
    private final RedisTemplate<String, Integer> userIdToPlanIdRedis;
    private static final String USERIDTOPLANID_PREFIX = "USERIDTOPLANID";

    public void registerUserIdToPlanId(int planId, int userId){
        userIdToPlanIdRedis.opsForValue().set(USERIDTOPLANID_PREFIX + userId, planId);
    }
    public Integer getPlanIdByUserId(int userId){
        return userIdToPlanIdRedis.opsForValue().get(USERIDTOPLANID_PREFIX + userId);
    }
    public Integer removeUserIdToPlanId(int userId){
        return userIdToPlanIdRedis.opsForValue().getAndDelete(USERIDTOPLANID_PREFIX + userId);
    }
}
