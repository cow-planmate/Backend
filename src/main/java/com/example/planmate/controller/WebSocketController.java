package com.example.planmate.controller;

import com.example.planmate.wdto.WRequest;
import com.example.planmate.service.WebSocketPlanService;
import com.example.planmate.wdto.WResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final WebSocketPlanService webSocketPlanService;

    @MessageMapping("/plan/{planId}")
    @SendTo("/topic/plan/{planId}")
    public WResponse handlePlanUpdate(@PathVariable("planId") int planId, WRequest request) {
        System.out.println("수정된 플랜: " + request);
        WResponse response = webSocketPlanService.run(planId, request);
        response.setType(request.getType());
        response.setObject(request.getObject());
        return response;
    }
}