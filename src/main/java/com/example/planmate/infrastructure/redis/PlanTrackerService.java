package com.example.planmate.infrastructure.redis;

import static com.example.planmate.infrastructure.redis.RedisKeys.planTracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.planmate.domain.webSocket.valueObject.UserDayIndexVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanTrackerService {
    private final RedisTemplate<String, String> planTrackerRedis;
    private final NicknameCacheService nicknameCacheService;
    public boolean exists(int planId){ return planTrackerRedis.hasKey(planTracker(planId)); }
    public void register(int planId, int userId, int dayIndex){ planTrackerRedis.opsForHash().put(planTracker(planId), userId, dayIndex); }
    public void registerBulk(int planId, List<UserDayIndexVO> userDayIndexVOs){ for(UserDayIndexVO vo : userDayIndexVOs){ Integer userId = nicknameCacheService.getUserId(vo.getNickname()); if(userId!=null){ planTrackerRedis.opsForHash().put(planTracker(planId), userId, vo.getDayIndex()); } } }
    public List<UserDayIndexVO> get(int planId){ Map<Object,Object> entries = planTrackerRedis.opsForHash().entries(planTracker(planId)); if(entries.isEmpty()) return Collections.emptyList(); List<UserDayIndexVO> list = new ArrayList<>(entries.size()); for(Map.Entry<Object,Object> e: entries.entrySet()){ Integer userId=(Integer)e.getKey(); Integer dayIndex=(Integer)e.getValue(); String nickname=nicknameCacheService.getNickname(userId); list.add(new UserDayIndexVO(nickname, dayIndex)); } return list; }
    public void remove(int planId, int userId){ planTrackerRedis.opsForHash().delete(planTracker(planId), userId); }
}
