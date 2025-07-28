package com.example.planmate.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class WebSocketEventTracker {

    private final RedisTemplate<String, String> trackerRedis;
    private final String PREFIX = "ws:sub:";

    private String topicKey(String destination) {
        return PREFIX + destination;
    }

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();
        if (destination != null && sessionId != null) {
            trackerRedis.opsForSet().add(topicKey(destination), sessionId);
        }
    }

    @EventListener
    public void handleUnsubscribeEvent(SessionUnsubscribeEvent event) {
        String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
        removeSessionFromAllTopics(sessionId);
    }

    @EventListener
    public void handleDisconnectEvent(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        removeSessionFromAllTopics(sessionId);
    }

    private void removeSessionFromAllTopics(String sessionId) {
        Set<String> keys = trackerRedis.keys(PREFIX + "*");
        for (String key : keys) {
            trackerRedis.opsForSet().remove(key, sessionId);
        }
    }

    public int getSubscriberCount(String destination) {
        Long size = trackerRedis.opsForSet().size(topicKey(destination));
        return size != null ? size.intValue() : 0;
    }
}

