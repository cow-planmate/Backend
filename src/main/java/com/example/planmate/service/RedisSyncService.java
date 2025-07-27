package com.example.planmate.service;

import com.example.planmate.entity.Plan;
import com.example.planmate.repository.PlanRepository;
import com.example.planmate.repository.TimeTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisSyncService {

    private final PlanRepository planRepository;
    private final TimeTableRepository timeTableRepository;
    private final RedisService redisService;
    private final SimpMessagingTemplate messagingTemplate;



    public void syncToDatabase(int planId) {
        syncPlanToDatabase(planId);
        syncTimetableToDatabase(planId);
        syncTimetablePlaceBlockToDatabase(planId);
    }

    public void syncPlanToDatabase(int planId) {
        Plan plan = redisService.getPlan(planId);
        planRepository.save(plan);
    }

    public void syncTimetableToDatabase(int planId){

    }

    public void syncTimetablePlaceBlockToDatabase(int planId) {

    }




}
