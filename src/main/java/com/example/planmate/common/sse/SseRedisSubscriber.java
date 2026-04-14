package com.example.planmate.common.sse;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Listens on the Redis SSE channel and delivers incoming notifications
 * to SSE emitters that are connected locally on this node.
 *
 * Every backend node subscribes to the same channel, so a notification
 * published by any node reaches all nodes; each node then fans out only
 * to its own in-memory emitters for the target user.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SseRedisSubscriber implements MessageListener {

    private final SseEmitterService sseEmitterService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            SseNotificationMessage msg = objectMapper.readValue(body, SseNotificationMessage.class);
            UUID userId = UUID.fromString(msg.getUserId());
            sseEmitterService.deliverToLocalEmitters(userId, msg.getEventName(), msg.getData());
        } catch (Exception e) {
            log.warn("SSE Redis 메시지 처리 실패: {}", e.getMessage());
        }
    }
}
