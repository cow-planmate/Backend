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
                executeAction(actionResponse, request.getPlanId());
            }
            
            log.info("Chat response generated successfully for user: {}", request.getUserId());
            
            // 사용자에게는 친근한 메시지만 반환, 액션 정보도 포함
            if (actionResponse.isHasAction()) {
                return ResponseEntity.ok(ChatBotResponse.successWithAction(
                    actionResponse.getUserMessage(), 
                    actionResponse.getAction()
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
    private void executeAction(ChatBotActionResponse actionResponse, Integer planId) {
        try {
            ChatBotActionResponse.ActionData action = actionResponse.getAction();
            String actionType = action.getAction();
            String targetName = action.getTargetName();
            
            // Plan 액션 처리 (update만 허용)
            if ("plan".equals(targetName)) {
                if ("update".equals(actionType)) {
                    WPlanRequest request = (WPlanRequest) action.getTarget();
                    var response = webSocketPlanService.updatePlan(planId, request);
                    
                    messagingTemplate.convertAndSend(
                        "/topic/plan/" + planId + "/update/plan", 
                        response
                    );
                    
                    log.info("Executed plan update action via ChatBot for planId: {}", planId);
                } else {
                    log.warn("Plan only supports update action, received: {}", actionType);
                }
            }
            
            // TimeTable 액션 처리 (create, update, delete)
            else if ("timeTable".equals(targetName)) {
                WTimetableRequest request = (WTimetableRequest) action.getTarget();
                
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
            }
            
            // TimeTablePlaceBlock 액션 처리 (create, update, delete)
            else if ("timeTablePlaceBlock".equals(targetName)) {
                WTimeTablePlaceBlockRequest request = (WTimeTablePlaceBlockRequest) action.getTarget();
                
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
            }
            
            else {
                log.warn("Unsupported target: {}", targetName);
            }
            
        } catch (Exception e) {
            log.error("Failed to execute ChatBot action: {}", e.getMessage());
        }
    }
}
