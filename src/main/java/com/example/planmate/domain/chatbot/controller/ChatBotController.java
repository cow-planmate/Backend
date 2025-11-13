package com.example.planmate.domain.chatbot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.domain.chatbot.dto.ChatBotActionResponse;
import com.example.planmate.domain.chatbot.dto.ChatBotRequest;
import com.example.planmate.domain.chatbot.dto.ChatBotResponse;
import com.example.planmate.domain.chatbot.service.ChatBotService;
import com.example.planmate.domain.webSocket.dto.WPlanRequest;
import com.example.planmate.domain.webSocket.dto.WPlanResponse;
import com.example.planmate.domain.webSocket.dto.WTimeTablePlaceBlockRequest;
import com.example.planmate.domain.webSocket.dto.WTimetableRequest;
import com.example.planmate.domain.webSocket.service.WebSocketPlanService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chatbot")
public class ChatBotController {
    
    private final ChatBotService chatBotService;
    private final WebSocketPlanService webSocketPlanService;
    private final SimpMessagingTemplate messagingTemplate;
    
    @PostMapping("/chat")
    public ResponseEntity<ChatBotResponse> chat(@RequestBody ChatBotRequest request) {
        try {
            
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ChatBotResponse.error("메시지를 입력해주세요."));
            }
            
            // 계획 컨텍스트 생성 (현재는 간단하게, 나중에 확장 가능)
            String planContext = request.getPlanId() != null ? 
                "계획 ID: " + request.getPlanId() : null;
            
            ChatBotActionResponse actionResponse = chatBotService.getChatResponse(
                request.getMessage(), 
                request.getPlanId(), 
                planContext
            );
            
            // 액션이 있다면 실제 데이터 변경 및 WebSocket 브로드캐스트
            if (actionResponse.isHasAction()) {
                executeActions(actionResponse, request.getPlanId());
            }
            
            log.info("Chat response generated successfully for user: {}", request.getUserId());
            
            // 사용자에게는 친근한 메시지만 반환, 액션 정보도 포함
            if (actionResponse.isHasAction()) {
                return ResponseEntity.ok(ChatBotResponse.successWithActions(
                    actionResponse.getUserMessage(), 
                    actionResponse.getActions()
                ));
            } else {
                return ResponseEntity.ok(ChatBotResponse.success(actionResponse.getUserMessage()));
            }
            
        } catch (Exception e) {
            log.error("Error processing chat request: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ChatBotResponse.error("서버 내부 오류가 발생했습니다."));
        }
    }
    
    /**
     * AI가 요청한 액션을 실제로 실행하고 WebSocket으로 브로드캐스트
     */
    private void executeActions(ChatBotActionResponse actionResponse, Integer planId) {
        if (actionResponse.getActions() == null || actionResponse.getActions().isEmpty()) {
            log.warn("Action response marked as hasAction but no actions were provided.");
            return;
        }

        for (ChatBotActionResponse.ActionData action : actionResponse.getActions()) {
            try {
                handleAction(action, planId);
            } catch (Exception e) {
                log.error("Failed to execute ChatBot action: {}", e.getMessage(), e);
            }
        }
    }

    private void handleAction(ChatBotActionResponse.ActionData action, Integer planId) {
        if (action == null) {
            log.warn("Received null action from ChatBotActionResponse.");
            return;
        }

        String actionType = action.getAction();
        String targetName = action.getTargetName();

        // Plan 액션 처리 (update만 허용)
        if ("plan".equals(targetName)) {
            if (!(action.getTarget() instanceof WPlanRequest request)) {
                log.warn("Expected WPlanRequest target for plan action but received: {}",
                    action.getTarget() != null ? action.getTarget().getClass().getName() : "null");
                return;
            }

            if ("update".equals(actionType)) {
                WPlanResponse response = webSocketPlanService.updatePlan(planId, request);

                messagingTemplate.convertAndSend(
                    "/topic/plan/" + planId + "/update/plan",
                    response
                );

                log.info("Executed plan update action via ChatBot for planId: {}", planId);
            } else {
                log.warn("Plan only supports update action, received: {}", actionType);
            }
            return;
        }

        // TimeTable 액션 처리 (create, update, delete)
        if ("timeTable".equals(targetName)) {
            if (!(action.getTarget() instanceof WTimetableRequest request)) {
                log.warn("Expected WTimetableRequest target for timetable action but received: {}",
                    action.getTarget() != null ? action.getTarget().getClass().getName() : "null");
                return;
            }

            switch (actionType) {
                case "create":
                    var createResponse = webSocketPlanService.createTimetable(planId, request);
                    messagingTemplate.convertAndSend(
                        "/topic/plan/" + planId + "/create/timetable",
                        createResponse
                    );
                    log.info("Executed timetable create action via ChatBot for planId: {}", planId);
                    break;

                case "update":
                    var updateResponse = webSocketPlanService.updateTimetable(planId, request);
                    messagingTemplate.convertAndSend(
                        "/topic/plan/" + planId + "/update/timetable",
                        updateResponse
                    );
                    log.info("Executed timetable update action via ChatBot for planId: {}", planId);
                    break;

                case "delete":
                    var deleteResponse = webSocketPlanService.deleteTimetable(planId, request);
                    messagingTemplate.convertAndSend(
                        "/topic/plan/" + planId + "/delete/timetable",
                        deleteResponse
                    );
                    log.info("Executed timetable delete action via ChatBot for planId: {}", planId);
                    break;

                default:
                    log.warn("Unsupported timeTable action: {}", actionType);
            }
            return;
        }

        // TimeTablePlaceBlock 액션 처리 (create, update, delete)
        if ("timeTablePlaceBlock".equals(targetName)) {
            if (!(action.getTarget() instanceof WTimeTablePlaceBlockRequest request)) {
                log.warn("Expected WTimeTablePlaceBlockRequest target for place block action but received: {}",
                    action.getTarget() != null ? action.getTarget().getClass().getName() : "null");
                return;
            }

            switch (actionType) {
                case "create":
                    var createResponse = webSocketPlanService.createTimetablePlaceBlock(request);
                    messagingTemplate.convertAndSend(
                        "/topic/plan/" + planId + "/create/timetableplaceblock",
                        createResponse
                    );
                    log.info("Executed place block create action via ChatBot for planId: {}", planId);
                    break;

                case "update":
                    var updateResponse = webSocketPlanService.updateTimetablePlaceBlock(request);
                    messagingTemplate.convertAndSend(
                        "/topic/plan/" + planId + "/update/timetableplaceblock",
                        updateResponse
                    );
                    log.info("Executed place block update action via ChatBot for planId: {}", planId);
                    break;

                case "delete":
                    var deleteResponse = webSocketPlanService.deleteTimetablePlaceBlock(request);
                    messagingTemplate.convertAndSend(
                        "/topic/plan/" + planId + "/delete/timetableplaceblock",
                        deleteResponse
                    );
                    log.info("Executed place block delete action via ChatBot for planId: {}", planId);
                    break;

                default:
                    log.warn("Unsupported timeTablePlaceBlock action: {}", actionType);
            }
            return;
        }

        log.warn("Unsupported target: {}", targetName);
    }
}
