package com.example.planmate.service;

import com.example.planmate.dto.WebSocketChangeIdResponse;
import com.example.planmate.entity.Plan;
import com.example.planmate.entity.TimeTable;
import com.example.planmate.repository.PlanRepository;
import com.example.planmate.repository.TimeTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CacheSyncService {

    private final PlanRepository planRepository;
    private final TimeTableRepository timeTableRepository;
    private final CacheService cacheService;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 20 * 1000)
    public void syncPlanToDatabase() {
        RedisTemplate<String, Plan> planRedis = cacheService.getPlanRedis();

        Set<String> keys = planRedis.keys("*");
        List<Plan> plans = keys.stream()
                .map(k -> planRedis.opsForValue().get(k))
                .toList();
        planRepository.saveAll(plans);
    }

    @Scheduled(fixedRate = 20 * 1000)
    public void syncTimetableToDatabase(){
        RedisTemplate<String, TimeTable> timetableRedis = cacheService.getTimetableRedis();
        Set<String> keys = timetableRedis.keys("*");
        List<TimeTable> timeTables = keys.stream()
                .map(k -> timetableRedis.opsForValue().get(k))
                .toList();
        timetableRedis.delete(keys);
        timeTableRepository.saveAll(timeTables);

        Map<Integer, TimeTable> changeTimetableId = new HashMap<>();
        for (TimeTable timeTable : timeTables) {
            if(timeTable.getTimeTableTempId() != null){
                changeTimetableId.put(timeTable.getTimeTableTempId(), timeTable);
            }
        }

        Map<Integer, Map<Integer, Integer>> planToIdMap = new HashMap<>();

        for (Map.Entry<Integer, TimeTable> entry : changeTimetableId.entrySet()) {
            Integer tempId = entry.getKey();
            Integer realId = entry.getValue().getTimeTableTempId();
            Integer planId = entry.getValue().getPlan().getPlanId();
            planToIdMap.computeIfAbsent(planId, k -> new HashMap<>()).put(tempId, realId);
        }

        for (Map.Entry<Integer, Map<Integer, Integer>> entry : planToIdMap.entrySet()) {
            Integer planId = entry.getKey();
            Map<Integer, Integer> idMap = entry.getValue();

            WebSocketChangeIdResponse response = new WebSocketChangeIdResponse();
            response.setType("UPDATEID");
            response.setObject("Timetable");
            response.setMap(idMap);

            messagingTemplate.convertAndSend(
                    "/topic/plan/" + planId,
                    response
            );
        }

    }


}
