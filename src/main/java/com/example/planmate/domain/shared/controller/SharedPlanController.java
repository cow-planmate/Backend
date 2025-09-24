package com.example.planmate.domain.shared.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.example.planmate.domain.shared.dto.*;
import com.example.planmate.domain.shared.service.sharedService.SharedPlanService;

@Controller
@RequiredArgsConstructor
public class SharedPlanController {
    private final SharedPlanService sharedPlanService;

    @MessageMapping("/plan/{planId}/update/plan")
    @SendTo("/topic/plan/{planId}/update/plan")
    public WPlanResponse updatePlan(@DestinationVariable int planId, @Payload WPlanRequest request) {
        request.setPlanId(planId);
        WPlanResponse response = sharedPlanService.update(request);
        response.setEventId(request.getEventId() == null ? "" : request.getEventId());
        return response;
    }
    

    


}