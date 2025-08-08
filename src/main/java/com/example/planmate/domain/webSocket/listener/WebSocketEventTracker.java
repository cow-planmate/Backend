package com.example.planmate.domain.webSocket.listener;

import com.example.planmate.domain.webSocket.service.RedisService;
import com.example.planmate.domain.webSocket.service.RedisSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventTracker {

    private final RedisTemplate<String, String> planTrackerRedis;
    private final String PLANTRACKER_PREFIX = "PLANTRACKER";
    private final RedisTemplate<String, Integer> sessionTrackerRedis;
    private final String SESSIONTRACKER_PREFIX = "SESSIONTRACKER";
    private final RedisService redisService;
    private final RedisSyncService redisSyncService;


    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();
        int planId = Integer.parseInt(destination.split("/")[3]);
        if(!planTrackerRedis.hasKey(PLANTRACKER_PREFIX + planId)){
            redisService.registerPlan(planId);
        }
        planTrackerRedis.opsForSet().add(PLANTRACKER_PREFIX + planId, sessionId);
        sessionTrackerRedis.opsForValue().set(SESSIONTRACKER_PREFIX + sessionId, planId);
    }

    @EventListener
    public void handleUnsubscribeEvent(SessionUnsubscribeEvent event) {

    }

    @EventListener
    public void handleDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        removeSessionFromAllTopics(sessionId);
    }

    private void removeSessionFromAllTopics(String sessionId) {
        int planId = sessionTrackerRedis.opsForValue().get(SESSIONTRACKER_PREFIX + sessionId);
        planTrackerRedis.opsForSet().remove(PLANTRACKER_PREFIX + planId, sessionId);
        sessionTrackerRedis.delete(SESSIONTRACKER_PREFIX + sessionId);
        if(!planTrackerRedis.hasKey(PLANTRACKER_PREFIX + planId)){
            redisSyncService.syncPlanToDatabase(planId);
        }
    }

    public int getSubscriberCount(int planId) {
        Long size = planTrackerRedis.opsForSet().size(PLANTRACKER_PREFIX + planId);
        return size != null ? size.intValue() : 0;
    }
}

