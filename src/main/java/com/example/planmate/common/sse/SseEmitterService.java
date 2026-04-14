package com.example.planmate.common.sse;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SseEmitterService {

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30분

    // Multiple emitters per user to support multiple tabs / simultaneous connections
    private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

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

    public void sendNotification(UUID userId, String eventName, Object data) {
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
