package com.example.planmate.domain.shared.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Payload;

import com.example.planmate.domain.shared.dto.WRequest;
import com.example.planmate.domain.shared.dto.WResponse;
import com.example.planmate.domain.shared.service.SharedService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class SharedContoller <req extends WRequest, res extends WResponse, T extends SharedService<req, res>> {
    protected final T service;

    // Reusable handlers for children to call in their annotated endpoints
    protected res handleCreate(@DestinationVariable int rootEntityId, @Payload req request) {
        res response = service.create(request);
        response.setEventId(request.getEventId() == null ? "" : request.getEventId());
        return response;
    }

    protected res handleUpdate(@DestinationVariable int rootEntityId, @Payload req request) {
        res response = service.update(request);
        response.setEventId(request.getEventId() == null ? "" : request.getEventId());
        return response;
    }

    protected res handleDelete(@DestinationVariable int rootEntityId, @Payload req request) {
        res response = service.delete(request);
        response.setEventId(request.getEventId() == null ? "" : request.getEventId());
        return response;
    }

}
