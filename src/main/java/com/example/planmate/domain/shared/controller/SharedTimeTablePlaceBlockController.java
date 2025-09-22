package com.example.planmate.domain.shared.controller;

import org.checkerframework.checker.units.qual.C;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.example.planmate.domain.shared.dto.WTimeTablePlaceBlockRequest;
import com.example.planmate.domain.shared.dto.WTimeTablePlaceBlockResponse;
import com.example.planmate.domain.shared.service.SharedTimeTablePlaceBlockService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SharedTimeTablePlaceBlockController {

    private final SharedTimeTablePlaceBlockService sharedTimeTablePlaceBlockService;

    @MessageMapping("/plan/{planId}/create/timetableplaceblock")
    @SendTo("/topic/plan/{planId}/create/timetableplaceblock")
    public WTimeTablePlaceBlockResponse createTimeTablePlaceBlock(@DestinationVariable int planId, @Payload WTimeTablePlaceBlockRequest request) {
        WTimeTablePlaceBlockResponse response = sharedTimeTablePlaceBlockService.createTimetablePlaceBlock(request);
        response.setEventId(request.getEventId() == null ? "" : request.getEventId());
        return response;
    }

    @MessageMapping("/plan/{planId}/update/timetableplaceblock")
    @SendTo("/topic/plan/{planId}/update/timetableplaceblock")
    public WTimeTablePlaceBlockResponse updateTimeTablePlaceBlock(@DestinationVariable int planId, @Payload WTimeTablePlaceBlockRequest request) {
        WTimeTablePlaceBlockResponse response = sharedTimeTablePlaceBlockService.updateTimetablePlaceBlock(request);
        response.setEventId(request.getEventId() == null ? "" : request.getEventId());
        return response;
    }

    @MessageMapping("/plan/{planId}/delete/timetableplaceblock")
    @SendTo("/topic/plan/{planId}/delete/timetableplaceblock")
    public WTimeTablePlaceBlockResponse deleteTimeTablePlaceBlock(@DestinationVariable int planId, @Payload WTimeTablePlaceBlockRequest request) {
        WTimeTablePlaceBlockResponse response = sharedTimeTablePlaceBlockService.deleteTimetablePlaceBlock(request);
        response.setEventId(request.getEventId() == null ? "" : request.getEventId());
        return response;
    }
}   
