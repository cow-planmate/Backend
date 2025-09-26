package com.example.planmate.domain.shared.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.example.planmate.domain.shared.dto.WRequest;
import com.example.planmate.domain.shared.dto.WResponse;
import com.example.planmate.domain.shared.service.SharedService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public abstract class SharedContoller <req extends WRequest, res extends WResponse, T extends SharedService<req, res>> {
    protected final T service;
    
    // 자식 클래스에서 엔티티 이름을 정의
    protected abstract String getEntityName();
    protected abstract String getRootEntityName();

    // WebSocket 메시징용 공통 메서드들
    private res handleCreate(@DestinationVariable int rootEntityId, @Payload req request) {
        res response = service.create(request);
        response.setEventId(request.getEventId() == null ? "" : request.getEventId());
        return response;
    }

    private res handleUpdate(@DestinationVariable int rootEntityId, @Payload req request) {
        res response = service.update(request);
        response.setEventId(request.getEventId() == null ? "" : request.getEventId());
        return response;
    }

    private res handleDelete(@DestinationVariable int rootEntityId, @Payload req request) {
        res response = service.delete(request);
        response.setEventId(request.getEventId() == null ? "" : request.getEventId());
        return response;
    }

    // 공통 WebSocket 엔드포인트들
    @MessageMapping("/{rootEntityName}/{rootEntityId}/create/{entityName}")
    @SendTo("/topic/{rootEntityName}/{rootEntityId}/create/{entityName}")  
    public res createEntity(@DestinationVariable String rootEntityName, @DestinationVariable int rootEntityId, @DestinationVariable String entityName, @Payload req request) {
        if (!rootEntityName.equals(getRootEntityName()) || !entityName.equals(getEntityName())) {
            throw new IllegalArgumentException("Entity name mismatch");
        }
        return handleCreate(rootEntityId, request);
    }

    @MessageMapping("/{rootEntityName}/{rootEntityId}/update/{entityName}")
    @SendTo("/topic/{rootEntityName}/{rootEntityId}/update/{entityName}")
    public res updateEntity(@DestinationVariable String rootEntityName, @DestinationVariable int rootEntityId, @DestinationVariable String entityName, @Payload req request) {
        if (!rootEntityName.equals(getRootEntityName()) || !entityName.equals(getEntityName())) {
            throw new IllegalArgumentException("Entity name mismatch");
        }
        return handleUpdate(rootEntityId, request);
    }

    @MessageMapping("/{rootEntityName}/{rootEntityId}/delete/{entityName}")
    @SendTo("/topic/{rootEntityName}/{rootEntityId}/delete/{entityName}")
    public res deleteEntity(@DestinationVariable String rootEntityName, @DestinationVariable int rootEntityId, @DestinationVariable String entityName, @Payload req request) {
        if (!rootEntityName.equals(getRootEntityName()) || !entityName.equals(getEntityName())) {
            throw new IllegalArgumentException("Entity name mismatch");
        }
        return handleDelete(rootEntityId, request);
    }

}
