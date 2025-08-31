package com.example.planmate.domain.webSocket.listener;

import com.example.planmate.domain.webSocket.dto.WPresenceResponse;
import com.example.planmate.domain.webSocket.enums.EAction;
import com.example.planmate.domain.webSocket.service.RedisService;
import com.example.planmate.domain.webSocket.service.RedisSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventTracker {


    private final String USER_ID = "userId";

    private final RedisService redisService;
    private final RedisSyncService redisSyncService;
    private final SimpMessagingTemplate messaging;

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Object v = accessor.getSessionAttributes().get(USER_ID);
        Integer userId = Integer.valueOf(String.valueOf(v));
        String destination = accessor.getDestination();
        int planId = Integer.parseInt(destination.split("/")[3]);
        if(!redisService.hasPlanTracker(planId)) {
            redisService.registerPlan(planId);
        }
        redisService.putPlanTracker(planId, userId, 0);
        redisService.registerNickname(userId);
        broadcastPresence(planId, userId, EAction.CREATE);
    }

    @EventListener
    public void handleDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Object v = accessor.getSessionAttributes().get(USER_ID);
        Integer userId = Integer.valueOf(String.valueOf(v));
        String destination = accessor.getDestination();
        int planId = Integer.parseInt(destination.split("/")[3]);
        removeSessionFromAllTopics(planId, userId);

    }

    private void removeSessionFromAllTopics(int planId, int userId) {
        redisService.removePlanTracker(planId, userId);
        redisService.removeNickname(userId);
        if(!redisService.hasPlanTracker(planId)){
            redisSyncService.syncPlanToDatabase(planId);
        }
        broadcastPresence(planId, userId, EAction.DELETE);
    }

    private void broadcastPresence(int planId, int userId, EAction action) {
        String nickname = redisService.getNicknameByUserId(userId);
        WPresenceResponse response = new WPresenceResponse(nickname, userId);
        messaging.convertAndSend("/topic/plan/" + planId + action.getValue() + "/presence", response);
    }
}

