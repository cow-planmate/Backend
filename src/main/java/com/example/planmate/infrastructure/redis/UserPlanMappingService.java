package com.example.planmate.infrastructure.redis;

import static com.example.planmate.infrastructure.redis.RedisKeys.userPlan;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserPlanMappingService {
    private final RedisTemplate<String, Integer> userIdToPlanIdRedis;
    public void register(int planId, int userId){ userIdToPlanIdRedis.opsForValue().set(userPlan(userId), planId); }
    public Integer getPlanId(int userId){ return userIdToPlanIdRedis.opsForValue().get(userPlan(userId)); }
    public Integer remove(int userId){ return userIdToPlanIdRedis.opsForValue().getAndDelete(userPlan(userId)); }
}
