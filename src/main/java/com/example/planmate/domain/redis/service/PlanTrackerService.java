package com.example.planmate.domain.redis.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.planmate.domain.webSocket.valueObject.UserDayIndexVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanTrackerService {
    @Qualifier("userIdNicknameRedis")
    private final RedisTemplate<String, String> userIdNicknameRedis;
    @Qualifier("planTrackerRedis")
    private final RedisTemplate<String, Integer> planTrackerRedis;

    private static final String PLANTRACKER_PREFIX = "PLANTRACKER";
    private static final String USERID_NICKNAME_PREFIX = "USERIDNICKNAME";

    public boolean hasPlanTracker(int planId) {
        return planTrackerRedis.hasKey(PLANTRACKER_PREFIX + planId);
    }

    public void registerPlanTracker(int planId, int userId, int dayIndex) {
        planTrackerRedis.opsForHash().put(PLANTRACKER_PREFIX + planId, userId, dayIndex);
    }

    public List<UserDayIndexVO> getPlanTracker(int planId) {
        String key = PLANTRACKER_PREFIX + planId;
        Map<Object, Object> entries = planTrackerRedis.opsForHash().entries(key);
        if (entries.isEmpty()) return Collections.emptyList();

        List<UserDayIndexVO> result = new ArrayList<>(entries.size());
        for (Map.Entry<Object, Object> e : entries.entrySet()) {
            Integer userId   = (Integer) e.getKey();     // hash field
            Integer dayIndex = (Integer) e.getValue();   // hash value
            String nickname  = userIdNicknameRedis.opsForValue().get(USERID_NICKNAME_PREFIX + userId);
            result.add(new UserDayIndexVO(nickname, dayIndex));
        }
        return result;
    }

    public void removePlanTracker(int planId, int userId) {
        planTrackerRedis.opsForHash().delete(PLANTRACKER_PREFIX + planId, userId);
    }
}
