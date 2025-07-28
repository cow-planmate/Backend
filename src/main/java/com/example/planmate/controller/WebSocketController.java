package com.example.planmate.controller;

import com.example.planmate.service.WebSocketPlanService;
import com.example.planmate.wdto.WRequest;
import com.example.planmate.wdto.WResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final WebSocketPlanService webSocketPlanService;

    @MessageMapping("/plan/{planId}")
    @SendTo("/topic/plan/{planId}")
    public WResponse handlePlanUpdate(@DestinationVariable("planId") int planId, Principal principal, WRequest request) {
        WResponse response = webSocketPlanService.run(planId, request);
        response.setType(request.getType());
        response.setObject(request.getObject());
        return response;
    }
}