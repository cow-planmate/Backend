package com.example.planmate.domain.shared.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.example.planmate.domain.shared.dto.WPresencesRequest;
import com.example.planmate.domain.shared.dto.WPresencesResponse;
import com.example.planmate.domain.shared.service.PresenceTrackingService;

import lombok.RequiredArgsConstructor;
@Controller
@RequiredArgsConstructor
public class PresenceTrackingController {

    private final PresenceTrackingService presenceTrackingService;

    @MessageMapping("/plan/{planId}/update/presence")
    @SendTo("/topic/plan/{planId}/update/presence")
    public WPresencesResponse updatePresence(@DestinationVariable int planId, @Payload WPresencesRequest request) {
        WPresencesResponse response = presenceTrackingService.updatePresence(planId, request);
        response.setUserDayIndexVOs(request.getUserDayIndexVO());
        return response;
    }
}
