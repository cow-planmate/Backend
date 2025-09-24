package com.example.planmate.domain.shared.listener;

import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import com.example.planmate.domain.shared.cache.PlanCache;
import com.example.planmate.domain.shared.cache.TimeTableCache;
import com.example.planmate.domain.shared.cache.TimeTablePlaceBlockCache;
import com.example.planmate.domain.shared.dto.WPresenceResponse;
import com.example.planmate.domain.shared.enums.EAction;
import com.example.planmate.domain.shared.lazydto.TimeTableDto;
import com.example.planmate.domain.shared.service.PresenceTrackingService;
import com.example.planmate.domain.shared.service.sync.CacheSyncService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SharedEventTracker {


    private final String USER_ID = "userId";

    private final PlanCache planCache;
    private final TimeTableCache timeTableCache;
    private final TimeTablePlaceBlockCache timeTablePlaceBlockCache;
    private final PresenceTrackingService presenceTrackingService;
    private final CacheSyncService redisSyncService;
    private final SimpMessagingTemplate messaging;

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Object v = accessor.getSessionAttributes().get(USER_ID);
        Integer userId = Integer.valueOf(String.valueOf(v));
        String destination = accessor.getDestination();
        int planId = Integer.parseInt(destination.split("/")[3]);
        if(!presenceTrackingService.hasPlanTracker(planId)) {
            planCache.insertPlanByKey(planId);
            List<TimeTableDto> timeTables = timeTableCache.insertTimeTablesByPlanId(planId);
            for(TimeTableDto timeTable : timeTables){
                timeTablePlaceBlockCache.insertTimeTablePlaceBlock(timeTable.timeTableId());
            }
        }
        presenceTrackingService.insertPlanTracker(planId, userId, 0);
        presenceTrackingService.insertNickname(userId);
        presenceTrackingService.insertUserIdToPlanId(planId, userId);
        broadcastPresence(planId, userId, EAction.CREATE);
    }

    @EventListener
    public void handleDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Object v = accessor.getSessionAttributes().get(USER_ID);
        Integer userId = Integer.valueOf(String.valueOf(v));
        int planId = presenceTrackingService.removeUserIdToPlanId(userId);
        removeSessionFromAllTopics(planId, userId);

    }

    private void removeSessionFromAllTopics(int planId, int userId) {
        presenceTrackingService.removePlanTracker(planId, userId);
        presenceTrackingService.removeNickname(userId);
        if(!presenceTrackingService.hasPlanTracker(planId)){
            redisSyncService.syncPlanToDatabase(planId);
        }
        broadcastPresence(planId, userId, EAction.DELETE);
    }

    private void broadcastPresence(int planId, int userId, EAction action) {
        String nickname = presenceTrackingService.getNicknameByUserId(userId);
        WPresenceResponse response = new WPresenceResponse(nickname, userId);
        messaging.convertAndSend("/topic/plan/" + planId + action.getValue() + "/presence", response);
    }
}

