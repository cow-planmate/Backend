package com.example.planmate.generated.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.example.planmate.generated.dto.WPresencesRequest;
import com.example.planmate.generated.dto.WPresencesResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.sharedsync.framework.shared.enums.ECasheKey;
import com.example.planmate.generated.valueObject.UserDayIndexVO;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PresenceTrackingService {

    private final RedisTemplate<String, String> planTrackerRedis;
    private final RedisTemplate<String, String> userIdNicknameRedis;
    private final RedisTemplate<String, Integer> nicknameUseridRedis;
    private final RedisTemplate<String, Integer> userIdToPlanIdRedis;
    private final UserRepository userRepository;

    public boolean hasPlanTracker(int planId) {
        return planTrackerRedis.hasKey(ECasheKey.PLANTRACKER.key(planId));
    }

    public void insertPlanTracker(int planId, int userId, int dayIndex) {
        planTrackerRedis.opsForHash().put(ECasheKey.PLANTRACKER.key(planId), userId, dayIndex);
    }

    public void insertPlanTracker(int planId, List<UserDayIndexVO> userDayIndexVOs) {
        for (UserDayIndexVO userDayIndexVO : userDayIndexVOs) {
            Integer userId = getUserIdByNickname(userDayIndexVO.getNickname());
            if (userId != null) {
                planTrackerRedis.opsForHash().put(ECasheKey.PLANTRACKER.key(planId), userId, userDayIndexVO.getDayIndex());
            }
        }
    }

    public List<UserDayIndexVO> getPlanTracker(int planId) {
        String key = ECasheKey.PLANTRACKER.key(planId);
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
        planTrackerRedis.opsForHash().delete(ECasheKey.PLANTRACKER.key(planId), userId);
    }

    public String getNicknameByUserId(int userId) {
        return userIdNicknameRedis.opsForValue().get(ECasheKey.USERIDNICKNAME.key(userId));
    }

    public Integer getUserIdByNickname(String nickname) {
        return nicknameUseridRedis.opsForValue().get(ECasheKey.NICKNAMEUSERID.key(nickname));
    }

    public void insertNickname(int userId, String nickname) {
        userIdNicknameRedis.opsForValue().set(ECasheKey.USERIDNICKNAME.key(userId), nickname);
    }

    public void insertNickname(int userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            userIdNicknameRedis.opsForValue().set(ECasheKey.USERIDNICKNAME.key(user.getUserId()), user.getNickname());
            nicknameUseridRedis.opsForValue().set(ECasheKey.NICKNAMEUSERID.key(user.getNickname()), user.getUserId());
        }
    }

    public void removeNickname(int userId) {
        String nickname = userIdNicknameRedis.opsForValue().getAndDelete(ECasheKey.USERIDNICKNAME.key(userId));
        if (nickname != null) {
            nicknameUseridRedis.delete(ECasheKey.NICKNAMEUSERID.key(nickname));
        }
    }

    public void insertUserIdToPlanId(int planId, int userId) {
        userIdToPlanIdRedis.opsForValue().set(ECasheKey.USERIDTOPLANID.key(userId), planId);
    }

    public int getPlanIdByUserId(int userId) {
        Integer v = userIdToPlanIdRedis.opsForValue().get(ECasheKey.USERIDTOPLANID.key(userId));
        return v == null ? 0 : v;
    }

    public int removeUserIdToPlanId(int userId) {
        Integer v = userIdToPlanIdRedis.opsForValue().getAndDelete(ECasheKey.USERIDTOPLANID.key(userId));
        return v == null ? 0 : v;
    }

    public WPresencesResponse updatePresence(int planId, WPresencesRequest request) {
        WPresencesResponse response = new WPresencesResponse();
        insertPlanTracker(planId, request.getUserDayIndexVO());
        response.setUserDayIndexVOs(request.getUserDayIndexVO());
        return response;
    }
}
