package com.sharedsync.framework.shared.listener;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SharedEventTracker {

    private static final String USER_ID = "userId";
    private final PresenceSessionManager presenceSessionManager;

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Integer userId = extractUserId(accessor);
        Integer roomId = parseRoomId(accessor.getDestination());

        if (userId != null && roomId != null) {
            presenceSessionManager.handleSubscribe(roomId, userId);
        }
    }

    @EventListener
    public void handleDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Integer userId = extractUserId(accessor);

        if (userId != null) {
            presenceSessionManager.handleDisconnect(userId);
        }
    }

    private Integer extractUserId(StompHeaderAccessor accessor) {
        Object value = accessor.getSessionAttributes().get(USER_ID);
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer parseRoomId(String destination) {
        if (destination == null) {
            return null;
        }
        String[] tokens = destination.split("/");
        if (tokens.length <= 2) {
            return null;
        }
        try {
            return Integer.parseInt(tokens[2]);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}