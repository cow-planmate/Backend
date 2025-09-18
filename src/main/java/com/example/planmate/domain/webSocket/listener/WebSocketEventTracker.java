package com.example.planmate.domain.webSocket.listener;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import com.example.planmate.domain.webSocket.dto.WPresenceResponse;
import com.example.planmate.domain.webSocket.enums.EAction;
import com.example.planmate.domain.webSocket.service.RedisSyncService;
import com.example.planmate.infrastructure.redis.NicknameCacheService;
import com.example.planmate.infrastructure.redis.PlanCacheService;
import com.example.planmate.infrastructure.redis.PlanTrackerService;
import com.example.planmate.infrastructure.redis.TimeTableCacheService;
import com.example.planmate.infrastructure.redis.TimeTablePlaceBlockCacheService;
import com.example.planmate.infrastructure.redis.UserPlanMappingService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketEventTracker {


    private final String USER_ID = "userId";

    private final PlanTrackerService planTrackerService;
    private final NicknameCacheService nicknameCacheService;
    private final UserPlanMappingService userPlanMappingService;
    private final RedisSyncService redisSyncService;
    private final SimpMessagingTemplate messaging;
    private final PlanCacheService planCacheService;
    private final TimeTableCacheService timeTableCacheService;
    private final TimeTablePlaceBlockCacheService blockCacheService;

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Object v = accessor.getSessionAttributes().get(USER_ID);
        Integer userId = Integer.valueOf(String.valueOf(v));
        String destination = accessor.getDestination();
        int planId = Integer.parseInt(destination.split("/")[3]);
        if(!planTrackerService.exists(planId)) { // initialize plan cache if needed
            planCacheService.loadPlan(planId);
            timeTableCacheService.loadForPlan(planId);
            timeTableCacheService.getByPlan(planId).forEach(tt -> blockCacheService.loadForTimeTable(tt.getTimeTableId()));
        }
        planTrackerService.register(planId, userId, 0);
        nicknameCacheService.register(userId);
        userPlanMappingService.register(planId, userId);
        broadcastPresence(planId, userId, EAction.CREATE);
    }

    @EventListener
    public void handleDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Object v = accessor.getSessionAttributes().get(USER_ID);
        Integer userId = Integer.valueOf(String.valueOf(v));
        int planId = userPlanMappingService.remove(userId);
        removeSessionFromAllTopics(planId, userId);

    }

    private void removeSessionFromAllTopics(int planId, int userId) {
        planTrackerService.remove(planId, userId);
        nicknameCacheService.remove(userId);
        if(!planTrackerService.exists(planId)){
            redisSyncService.syncPlanToDatabase(planId);
        }
        broadcastPresence(planId, userId, EAction.DELETE);
    }

    private void broadcastPresence(int planId, int userId, EAction action) {
        String nickname = nicknameCacheService.getNickname(userId);
        WPresenceResponse response = new WPresenceResponse(nickname, userId);
        messaging.convertAndSend("/topic/plan/" + planId + action.getValue() + "/presence", response);
    }
}

