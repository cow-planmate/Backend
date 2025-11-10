package com.example.planmate.generated.listener;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.example.planmate.generated.dto.WPresenceResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PresenceBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcast(int planId, int userId, String nickname, String action) {
        WPresenceResponse response = new WPresenceResponse(nickname, userId);
        messagingTemplate.convertAndSend("/topic/plan/" + planId + action + "/presence", response);
    }
}
