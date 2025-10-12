package com.example.planmate.domain.shared.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.example.planmate.domain.shared.dto.WTimeTablePlaceBlockRequest;
import com.example.planmate.domain.shared.dto.WTimeTablePlaceBlockResponse;
import com.example.planmate.domain.shared.framework.contoller.SharedContoller;
import com.example.planmate.domain.shared.service.SharedTimeTablePlaceBlockService;

@Controller
public class SharedTimeTablePlaceBlockController extends SharedContoller<WTimeTablePlaceBlockRequest, WTimeTablePlaceBlockResponse, SharedTimeTablePlaceBlockService> {

    public SharedTimeTablePlaceBlockController(SharedTimeTablePlaceBlockService service) {
        super(service);
    }

    @MessageMapping("/{roomId}/create/timetableplaceblock")
    @SendTo("/topic/{roomId}/create/timetableplaceblock")
    public WTimeTablePlaceBlockResponse create(@DestinationVariable int roomId, @Payload WTimeTablePlaceBlockRequest request) {
        return handleCreate(roomId, request);
    }

    @MessageMapping("/{roomId}/update/timetableplaceblock")
    @SendTo("/topic/{roomId}/update/timetableplaceblock")
    public WTimeTablePlaceBlockResponse update(@DestinationVariable int roomId, @Payload WTimeTablePlaceBlockRequest request) {
        return handleUpdate(roomId, request);
    }

    @MessageMapping("/{roomId}/delete/timetableplaceblock")
    @SendTo("/topic/{roomId}/delete/timetableplaceblock")
    public WTimeTablePlaceBlockResponse delete(@DestinationVariable int roomId, @Payload WTimeTablePlaceBlockRequest request) {
        return handleDelete(roomId, request);
    }
}
