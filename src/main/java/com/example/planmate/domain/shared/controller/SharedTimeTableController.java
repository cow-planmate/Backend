package com.example.planmate.domain.shared.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.example.planmate.domain.shared.dto.WTimetableRequest;
import com.example.planmate.domain.shared.dto.WTimetableResponse;
import com.example.planmate.domain.shared.service.SharedTimeTableService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SharedTimeTableController {

    private final SharedTimeTableService sharedTimeTableService;

    @MessageMapping("/plan/{planId}/create/timetable")
    @SendTo("/topic/plan/{planId}/create/timetable")
    public WTimetableResponse createTimetable(@DestinationVariable int planId, @Payload WTimetableRequest request) {
        WTimetableResponse response = sharedTimeTableService.createTimetable(planId, request);
        response.setEventId(request.getEventId() == null ? "" : request.getEventId());
        return response;
    }

    @MessageMapping("/plan/{planId}/update/timetable")
    @SendTo("/topic/plan/{planId}/update/timetable")
    public WTimetableResponse updateTimetable(@DestinationVariable int planId, @Payload WTimetableRequest request) {
        WTimetableResponse response = sharedTimeTableService.updateTimetable(planId, request);
        response.setEventId(request.getEventId() == null ? "" : request.getEventId());
        return response;
    }

    @MessageMapping("/plan/{planId}/delete/timetable")
    @SendTo("/topic/plan/{planId}/delete/timetable")
    public WTimetableResponse deleteTimetable(@DestinationVariable int planId, @Payload WTimetableRequest request) {
        WTimetableResponse response = sharedTimeTableService.deleteTimetable(planId, request);
        response.setEventId(request.getEventId() == null ? "" : request.getEventId());
        return response;
    }
}
