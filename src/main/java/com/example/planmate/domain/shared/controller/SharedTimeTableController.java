package com.example.planmate.domain.shared.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.example.planmate.domain.shared.dto.WTimetableRequest;
import com.example.planmate.domain.shared.dto.WTimetableResponse;
import com.example.planmate.domain.shared.service.SharedTimeTableService;

@Controller
public class SharedTimeTableController extends SharedContoller<WTimetableRequest, WTimetableResponse, SharedTimeTableService> {

    public SharedTimeTableController(SharedTimeTableService service) {
        super(service);
    }

    @MessageMapping("/{roomId}/create/timetable")
    @SendTo("/topic/{roomId}/create/timetable")
    public WTimetableResponse createTimetable(@DestinationVariable int roomId, @Payload WTimetableRequest request) {
        return handleCreate(roomId, request);
    }

    @MessageMapping("/{roomId}/update/timetable")
    @SendTo("/topic/{roomId}/update/timetable")
    public WTimetableResponse updateTimetable(@DestinationVariable int roomId, @Payload WTimetableRequest request) {
        return handleUpdate(roomId, request);
    }

    @MessageMapping("/{roomId}/delete/timetable")
    @SendTo("/topic/{roomId}/delete/timetable")
    public WTimetableResponse deleteTimetable(@DestinationVariable int roomId, @Payload WTimetableRequest request) {
        return handleDelete(roomId, request);
    }
}
