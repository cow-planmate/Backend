package com.sharedsync.framework.shared.presence.core;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PresenceBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcast(String entityName, int roomId, String action, Object dtoWithPresenceKeys) {
        Map<String, Object> payload = PresenceFieldScanner.extractPresenceData(dtoWithPresenceKeys);
        messagingTemplate.convertAndSend(
                String.format("/topic/%s/%d/%s/presence", entityName, roomId, action),
                payload
        );
    }
}
