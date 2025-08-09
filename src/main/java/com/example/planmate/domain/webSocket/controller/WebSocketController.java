package com.example.planmate.domain.webSocket.controller;

import com.example.planmate.domain.webSocket.dto.*;
import com.example.planmate.domain.webSocket.service.WebSocketPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final WebSocketPlanService webSocketPlanService;

    @MessageMapping("/plan/{planId}/update/plan")
    @SendTo("/topic/plan/{planId}/update/plan")
    public WPlanResponse updatePlan(@DestinationVariable int planId, @Payload WPlanRequest request) {
        return webSocketPlanService.updatePlan(planId, request);
    }

    @MessageMapping("/plan/{planId}/create/timetable")
    @SendTo("/topic/plan/{planId}/create/timetable")
    public WTimetableResponse createTimetable(@DestinationVariable int planId, @Payload WTimetableRequest request) {
        return webSocketPlanService.createTimetable(planId, request);
    }

    @MessageMapping("/plan/{planId}/update/timetable")
    @SendTo("/topic/plan/{planId}/update/timetable")
    public WTimetableResponse updateTimetable(@DestinationVariable int planId, @Payload WTimetableRequest request) {
        return webSocketPlanService.updateTimetable(planId, request);
    }

    @MessageMapping("/plan/{planId}/delete/timetable")
    @SendTo("/topic/plan/{planId}/delete/timetable")
    public WTimetableResponse deleteTimetable(@DestinationVariable int planId, @Payload WTimetableRequest request) {
        return webSocketPlanService.deleteTimetable(planId, request);
    }

    @MessageMapping("/plan/{planId}/create/timetableplaceblock")
    @SendTo("/topic/plan/{planId}/create/timetableplaceblock")
    public WTimeTablePlaceBlockResponse createTimeTablePlaceBlock(@DestinationVariable int planId, @Payload WTimeTablePlaceBlockRequest request) {
        return webSocketPlanService.createTimetablePlaceBlock(request);
    }

    @MessageMapping("/plan/{planId}/update/timetableplaceblock")
    @SendTo("/topic/plan/{planId}/update/timetableplaceblock")
    public WTimeTablePlaceBlockResponse updateTimeTablePlaceBlock(@DestinationVariable int planId, @Payload WTimeTablePlaceBlockRequest request) {
        return webSocketPlanService.updateTimetablePlaceBlock(request);
    }

    @MessageMapping("/plan/{planId}/delete/timetableplaceblock")
    @SendTo("/topic/plan/{planId}/delete/timetableplaceblock")
    public WTimeTablePlaceBlockResponse deleteTimeTablePlaceBlock(@DestinationVariable int planId, @Payload WTimeTablePlaceBlockRequest request) {
        return webSocketPlanService.deleteTimetablePlaceBlock(request);
    }


}