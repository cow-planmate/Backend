package com.example.planmate.generated.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.example.planmate.generated.dto.WTimeTablePlaceBlockRequest;
import com.example.planmate.generated.dto.WTimeTablePlaceBlockResponse;
import com.sharedsync.framework.shared.framework.contoller.SharedContoller;
import com.example.planmate.generated.service.SharedTimeTablePlaceBlockService;

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

    @MessageMapping("/{roomId}/read/timetableplaceblock")
    @SendTo("/topic/{roomId}/read/timetableplaceblock")
    public WTimeTablePlaceBlockResponse read(@DestinationVariable int roomId, @Payload WTimeTablePlaceBlockRequest request) {
        return handleRead(roomId, request);
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
