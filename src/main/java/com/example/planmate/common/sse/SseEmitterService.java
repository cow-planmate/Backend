package com.example.planmate.common.sse;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.planmate.common.config.SseRedisConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages SSE subscriptions and cross-node notification delivery.
 *
 * <h3>Multi-node fanout design</h3>
 * <ol>
 *   <li>{@link #sendNotification} serialises the event as JSON and publishes
 *       it to the Redis Pub/Sub channel {@code sse:notifications}.</li>
 *   <li>Every backend node's {@link SseRedisSubscriber} receives the message
 *       and calls {@link #deliverToLocalEmitters}, which writes to whichever
 *       SSE connections are attached on that node.</li>
 *   <li>Because delivery always goes through the Redis channel (including on
 *       the publishing node itself), there is no separate local-delivery code
 *       path and therefore no risk of duplicate events.</li>
 * </ol>
 *
 * If the Redis publish fails the event is delivered locally as a best-effort
 * fallback so that SSE still works during transient Redis outages.
 */
@Slf4j
@Service
public class SseEmitterService {

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30분

    // Multiple emitters per user to support multiple tabs / simultaneous connections
    private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    private final StringRedisTemplate sseStringRedisTemplate;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Autowired
    public SseEmitterService(@org.springframework.beans.factory.annotation.Qualifier("sseStringRedisTemplate") StringRedisTemplate sseStringRedisTemplate, ObjectMapper objectMapper) {
        this.sseStringRedisTemplate = sseStringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        CopyOnWriteArrayList<SseEmitter> userEmitters =
                emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());
        userEmitters.add(emitter);

        // Capture the specific emitter instance so callbacks only remove this one
        Runnable cleanup = () -> {
            CopyOnWriteArrayList<SseEmitter> list = emitters.get(userId);
            if (list != null) {
                list.remove(emitter);
                if (list.isEmpty()) {
                    emitters.remove(userId, list);
                }
            }
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            cleanup.run();
        }

        return emitter;
    }

    /**
     * Publishes an SSE event to all nodes via Redis Pub/Sub.
     *
     * Falls back to local-only delivery if the Redis publish fails so that
     * notifications still reach users connected to this node during outages.
     */
    public void sendNotification(UUID userId, String eventName, Object data) {
        SseNotificationMessage message = new SseNotificationMessage(userId.toString(), eventName, data);
        try {
            String json = objectMapper.writeValueAsString(message);
            sseStringRedisTemplate.convertAndSend(SseRedisConfig.SSE_CHANNEL, json);
        } catch (JsonProcessingException e) {
            log.warn("SSE 알림 직렬화 실패 (userId: {}, event: {}): {}", userId, eventName, e.getMessage());
            deliverToLocalEmitters(userId, eventName, data);
        } catch (Exception e) {
            log.warn("SSE Redis publish 실패 — 로컬 전달로 폴백 (userId: {}, event: {}): {}", userId, eventName, e.getMessage());
            deliverToLocalEmitters(userId, eventName, data);
        }
    }

    /**
     * Delivers an SSE event to emitters connected on this node only.
     * Called by {@link SseRedisSubscriber} after receiving a Redis message.
     */
    public void deliverToLocalEmitters(UUID userId, String eventName, Object data) {
        CopyOnWriteArrayList<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) {
            return;
        }

        userEmitters.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
                return false;
            } catch (IOException e) {
                log.warn("SSE 전송 실패 (userId: {}, event: {}): {}", userId, eventName, e.getMessage());
                return true;
            }
        });

        if (userEmitters.isEmpty()) {
            emitters.remove(userId, userEmitters);
        }
    }

    /**
     * Sends an SSE comment every 30 s to keep idle connections alive through
     * reverse proxies that would otherwise close quiet streams.
     */
    @Scheduled(fixedDelay = 30_000)
    public void sendHeartbeat() {
        emitters.forEach((userId, userEmitters) -> {
            userEmitters.removeIf(emitter -> {
                try {
                    emitter.send(SseEmitter.event().comment("heartbeat"));
                    return false;
                } catch (IOException e) {
                    log.debug("SSE heartbeat 실패 (userId: {}): {}", userId, e.getMessage());
                    return true;
                }
            });
            if (userEmitters.isEmpty()) {
                emitters.remove(userId, userEmitters);
            }
        });
    }
}
