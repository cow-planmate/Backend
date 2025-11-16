package com.example.planmate.generated.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.example.planmate.generated.dto.WPlanRequest;
import com.example.planmate.generated.dto.WPlanResponse;
import com.example.planmate.move.shared.framework.contoller.SharedContoller;
import com.example.planmate.generated.service.SharedPlanService;

@Controller
public class SharedPlanController extends SharedContoller<WPlanRequest, WPlanResponse, SharedPlanService> {

    public SharedPlanController(SharedPlanService service) {
        super(service);
    }

    @MessageMapping("/{roomId}/update/plan")
    @SendTo("/topic/{roomId}/update/plan")
    public WPlanResponse update(@DestinationVariable int roomId, @Payload WPlanRequest request) {
        return handleUpdate(roomId, request);
    }
}
