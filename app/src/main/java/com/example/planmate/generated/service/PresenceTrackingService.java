package com.example.planmate.generated.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;
import com.example.planmate.generated.dto.WPresencesRequest;
import com.example.planmate.generated.dto.WPresencesResponse;
import com.example.planmate.generated.valueObject.UserDayIndexVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PresenceTrackingService {

    private static final String PLAN_TRACKER_PREFIX = "PLANTRACKER";
    private static final String USER_ID_NICKNAME_PREFIX = "USERIDNICKNAME";
    private static final String NICKNAME_USER_ID_PREFIX = "NICKNAMEUSERID";
    private static final String USER_ID_TO_PLAN_ID_PREFIX = "USERIDTOPLANID";

    private final RedisTemplate<String, String> planTrackerRedis;
    private final RedisTemplate<String, String> userIdNicknameRedis;
    private final RedisTemplate<String, Integer> nicknameUseridRedis;
    private final RedisTemplate<String, Integer> userIdToPlanIdRedis;
    private final UserRepository userRepository;

    public boolean hasPlanTracker(int planId) {
        return planTrackerRedis.hasKey(planTrackerKey(planId));
    }

    public void insertPlanTracker(int planId, int userId, int dayIndex) {
        planTrackerRedis.opsForHash().put(planTrackerKey(planId), userId, dayIndex);
    }

    public void insertPlanTracker(int planId, List<UserDayIndexVO> userDayIndexVOs) {
        for (UserDayIndexVO userDayIndexVO : userDayIndexVOs) {
            Integer userId = getUserIdByNickname(userDayIndexVO.getNickname());
            if (userId != null) {
                planTrackerRedis.opsForHash().put(planTrackerKey(planId), userId, userDayIndexVO.getDayIndex());
            }
        }
    }

    public List<UserDayIndexVO> getPlanTracker(int planId) {
        String key = planTrackerKey(planId);
        Map<Object, Object> entries = planTrackerRedis.opsForHash().entries(key);
        if (entries.isEmpty()) return Collections.emptyList();

        List<UserDayIndexVO> result = new ArrayList<>(entries.size());
        for (Map.Entry<Object, Object> e : entries.entrySet()) {
            Integer userId   = (Integer) e.getKey();
            Integer dayIndex = (Integer) e.getValue();
            String nickname  = getNicknameByUserId(userId);
            result.add(new UserDayIndexVO(nickname, dayIndex));
        }
        return result;
    }

    public void removePlanTracker(int planId, int userId) {
        planTrackerRedis.opsForHash().delete(planTrackerKey(planId), userId);
    }

    public String getNicknameByUserId(int userId) {
        return userIdNicknameRedis.opsForValue().get(userIdNicknameKey(userId));
    }

    public Integer getUserIdByNickname(String nickname) {
        return nicknameUseridRedis.opsForValue().get(nicknameUserIdKey(nickname));
    }

    public void insertNickname(int userId, String nickname) {
        userIdNicknameRedis.opsForValue().set(userIdNicknameKey(userId), nickname);
    }

    public void insertNickname(int userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            userIdNicknameRedis.opsForValue().set(userIdNicknameKey(user.getUserId()), user.getNickname());
            nicknameUseridRedis.opsForValue().set(nicknameUserIdKey(user.getNickname()), user.getUserId());
        }
    }

    public void removeNickname(int userId) {
        String nickname = userIdNicknameRedis.opsForValue().getAndDelete(userIdNicknameKey(userId));
        if (nickname != null) {
            nicknameUseridRedis.delete(nicknameUserIdKey(nickname));
        }
    }

    public void insertUserIdToPlanId(int planId, int userId) {
        userIdToPlanIdRedis.opsForValue().set(userIdToPlanIdKey(userId), planId);
    }

    public int getPlanIdByUserId(int userId) {
        Integer v = userIdToPlanIdRedis.opsForValue().get(userIdToPlanIdKey(userId));
        return v == null ? 0 : v;
    }

    public int removeUserIdToPlanId(int userId) {
        Integer v = userIdToPlanIdRedis.opsForValue().getAndDelete(userIdToPlanIdKey(userId));
        return v == null ? 0 : v;
    }

    public WPresencesResponse updatePresence(int planId, WPresencesRequest request) {
        WPresencesResponse response = new WPresencesResponse();
        insertPlanTracker(planId, request.getUserDayIndexVO());
        response.setUserDayIndexVOs(request.getUserDayIndexVO());
        return response;
    }

    private static String planTrackerKey(int planId) {
        return PLAN_TRACKER_PREFIX + planId;
    }

    private static String userIdNicknameKey(int userId) {
        return USER_ID_NICKNAME_PREFIX + userId;
    }

    private static String nicknameUserIdKey(String nickname) {
        return NICKNAME_USER_ID_PREFIX + nickname;
    }

    private static String userIdToPlanIdKey(int userId) {
        return USER_ID_TO_PLAN_ID_PREFIX + userId;
    }
}
