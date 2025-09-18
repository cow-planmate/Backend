package com.example.planmate.domain.plan.service;

import static com.example.planmate.infrastructure.redis.RedisKeys.userPlan;

import org.springframework.data.redis.core.RedisTemplate;
// @Service removed to avoid duplicate bean with infrastructure.redis.UserPlanMappingService

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class UserPlanMappingService {

    private final RedisTemplate<String, Integer> userIdToPlanIdRedis; // key: USERIDTOPLANID+userId -> planId

    public void register(int planId, int userId){
        userIdToPlanIdRedis.opsForValue().set(userPlan(userId), planId);
    }

    public Integer getPlanId(int userId){
        return userIdToPlanIdRedis.opsForValue().get(userPlan(userId));
    }

    public Integer remove(int userId){
        return userIdToPlanIdRedis.opsForValue().getAndDelete(userPlan(userId));
    }
}
