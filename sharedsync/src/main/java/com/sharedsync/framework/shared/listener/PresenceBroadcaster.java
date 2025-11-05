package com.sharedsync.framework.shared.listener;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.sharedsync.framework.shared.dto.WPresenceResponse;
import com.sharedsync.framework.shared.enums.EAction;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PresenceBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcast(int planId, int userId, String nickname, EAction action) {
        WPresenceResponse response = new WPresenceResponse(nickname, userId);
        messagingTemplate.convertAndSend("/topic/plan/" + planId + action.getValue() + "/presence", response);
    }
}
