package com.example.planmate.domain.framework.controller;


import com.example.planmate.domain.framework.registry.SocketRootRegistry;
import com.example.planmate.domain.framework.service.SocketRootEntityService;
import com.example.planmate.domain.webSocket.dto.WPlanRequest;
import com.example.planmate.domain.webSocket.dto.WPlanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class SocketRootController {

    private final SocketRootEntityService rootEntityService;

    /**
     * ✅ 기존 WebSocketPlanController와 동일한 엔드포인트
     * 프론트에서 사용하는 경로:
     *   /app/plan/{planId}/update/plan
     * 구독 경로:
     *   /topic/plan/{planId}/update/plan
     */
    @MessageMapping("/plan/{planId}/update/plan")
    @SendTo("/topic/plan/{planId}/update/plan")
    public WPlanResponse updatePlan(
            @DestinationVariable int planId,
            @Payload WPlanRequest request
    ) {
        // Plan 루트 엔터티 클래스 조회
        Class<?> entityClass = SocketRootRegistry.getEntityClass("plan");

        // 프레임워크의 루트 엔터티 서비스 이용
        WPlanResponse response = new WPlanResponse();
        rootEntityService.updateRootEntity(entityClass, planId, request, response);

        // 기존 eventId 로직 그대로 유지
        response.setEventId(request.getEventId() == null ? "" : request.getEventId());
        return response;
    }
}

//@Controller
//@RequiredArgsConstructor
//public class SocketRootController {
//
//    private final SocketRootEntityService rootEntityService;
//
//    @MessageMapping("/update/{topic}/{id}")
//    @SendTo("/topic/{topic}/{id}/update")
//    public Object handleUpdate(
//            @DestinationVariable String topic,
//            @DestinationVariable int id,
//            @Payload Object request
//    ) {
//        Class<?> entityClass = SocketRootRegistry.getEntityClass(topic);
//        Object response = createResponseInstance(entityClass);
//        return rootEntityService.updateRootEntity(entityClass, id, request, response);
//    }
//
//    private Object createResponseInstance(Class<?> entityClass) {
//        try {
//            String responseName = "W" + entityClass.getSimpleName() + "Response";
//            String dtoPackage = "com.example.planmate.domain.webSocket.dto";
//            Class<?> responseClass = Class.forName(dtoPackage + "." + responseName);
//            return responseClass.getDeclaredConstructor().newInstance();
//        } catch (Exception e) {
//            throw new RuntimeException("Cannot create response DTO for " + entityClass.getSimpleName(), e);
//        }
//    }
//}