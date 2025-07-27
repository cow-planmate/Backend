package com.example.planmate.controller;

import com.example.planmate.dto.WebSocketPlanRequest;
import com.example.planmate.dto.WebSocketPlanResponse;
import com.example.planmate.service.WebSocketPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class PlanWebSocketController {
    private final WebSocketPlanService webSocketPlanService;

    @MessageMapping("/plan/{planId}")
    @SendTo("/topic/plan/{planId}")
    public WebSocketPlanResponse handlePlanUpdate(@PathVariable("planId") int planId, WebSocketPlanRequest request) {
        System.out.println("수정된 플랜: " + request);
        WebSocketPlanResponse response = webSocketPlanService.run(planId, request);
        response.setType(request.getType());
        response.setObject(request.getObject());
        return response;
    }
}