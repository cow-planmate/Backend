package com.example.planmate.domain.chatbot.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.domain.chatbot.dto.ChatBotActionResponse;
import com.example.planmate.domain.chatbot.dto.ChatBotRequest;
import com.example.planmate.domain.chatbot.dto.ChatBotResponse;
import com.example.planmate.domain.chatbot.service.ChatBotService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharedsync.shared.sync.RedisSyncService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sharedsync.dto.TimeTableDto;
import sharedsync.dto.TimeTablePlaceBlockDto;
import sharedsync.service.SharedPlanService;
import sharedsync.service.SharedTimeTablePlaceBlockService;
import sharedsync.service.SharedTimeTableService;
import sharedsync.wsdto.WPlanRequest;
import sharedsync.wsdto.WPlanResponse;
import sharedsync.wsdto.WTimeTablePlaceBlockRequest;
import sharedsync.wsdto.WTimeTableRequest;

@Tag(name = "Chatbot", description = "AI 챗봇 서비스 관련 API")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chatbot")
public class ChatBotController {
    
    private final ChatBotService chatBotService;
    private final SharedPlanService sharedPlanService;
    private final SharedTimeTableService sharedTimeTableService;
    private final SharedTimeTablePlaceBlockService sharedTimeTablePlaceBlockService;
    private final RedisSyncService redisSyncService;
    private final ObjectMapper objectMapper;
    
    @Operation(summary = "AI 대화 및 계획 수정", description = "사용자의 자연어 메시지를 분석하여 대답을 생성하고, 필요시 여행 계획을 자동으로 수정합니다.")
    @PostMapping("/chat")
    public ResponseEntity<ChatBotResponse> chat(@RequestBody ChatBotRequest request) {
        try {
            
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ChatBotResponse.error("메시지를 입력해주세요."));
            }

            ChatBotActionResponse actionResponse = chatBotService.getChatResponse(
                request.getMessage(),
                request.getPlanId()
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

        Map<Integer, Integer> tempTimetableIdMap = new HashMap<>();

        for (ChatBotActionResponse.ActionData action : actionResponse.getActions()) {
            try {
                handleAction(action, planId, tempTimetableIdMap);
            } catch (Exception e) {
                log.error("Failed to execute ChatBot action: {}", e.getMessage(), e);
            }
        }
    }

    private void handleAction(ChatBotActionResponse.ActionData action, Integer planId, Map<Integer, Integer> tempTimetableIdMap) {
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
                WPlanResponse response = sharedPlanService.update(request);

                redisSyncService.publish(
                    "/topic/" + planId,
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
            if (!(action.getTarget() instanceof WTimeTableRequest request)) {
                log.warn("Expected WTimeTableRequest target for timetable action but received: {}",
                    action.getTarget() != null ? action.getTarget().getClass().getName() : "null");
                return;
            }

            List<Integer> placeholderIds = new ArrayList<>();
            if (request.getTimeTableDtos() != null) {
                for (TimeTableDto dto : request.getTimeTableDtos()) {
                    if (dto != null && dto.getTimeTableId() != null && dto.getTimeTableId() < 0) {
                        placeholderIds.add(dto.getTimeTableId());
                    }
                }
            }

            switch (actionType) {
                case "create":
                    var createResponse = sharedTimeTableService.create(request);

                    if (!placeholderIds.isEmpty() && createResponse.getTimeTableDtos() != null) {
                        List<TimeTableDto> createdDtos = createResponse.getTimeTableDtos();
                        for (int i = 0; i < Math.min(placeholderIds.size(), createdDtos.size()); i++) {
                            Integer placeholderId = placeholderIds.get(i);
                            Integer realId = createdDtos.get(i).getTimeTableId();
                            if (placeholderId != null && realId != null) {
                                tempTimetableIdMap.put(placeholderId, realId);
                            }
                        }
                    }

                    redisSyncService.publish(
                        "/topic/" + planId,
                        createResponse
                    );
                    log.info("Executed timetable create action via ChatBot for planId: {}", planId);
                    break;

                case "update":
                    var updateResponse = sharedTimeTableService.update(request);
                    redisSyncService.publish(
                        "/topic/" + planId,
                        updateResponse
                    );
                    log.info("Executed timetable update action via ChatBot for planId: {}", planId);
                    break;

                case "delete":
                    var deleteResponse = sharedTimeTableService.delete(request);
                    redisSyncService.publish(
                        "/topic/" + planId,
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

            if (request.getTimeTablePlaceBlockDtos() != null) {
                for (TimeTablePlaceBlockDto dto : request.getTimeTablePlaceBlockDtos()) {
                    Map<String, Object> dtoMap = objectMapper.convertValue(dto, Map.class);
                    Integer ttId = (Integer) dtoMap.get("timeTableId");
                    if (dto != null && ttId != null && ttId != 0) {
                        Integer mappedId = tempTimetableIdMap.get(ttId);
                        if (mappedId != null) {
                            // Use reflection to set timeTableId since there's no setter
                            try {
                                java.lang.reflect.Field field = TimeTablePlaceBlockDto.class.getDeclaredField("timeTableId");
                                field.setAccessible(true);
                                field.set(dto, mappedId);
                            } catch (Exception e) {
                                log.error("Failed to set timeTableId via reflection", e);
                            }
                        }
                    }
                }
            }

            switch (actionType) {
                case "create":
                    var createResponse = sharedTimeTablePlaceBlockService.create(request);
                    redisSyncService.publish(
                        "/topic/" + planId,
                        createResponse
                    );
                    log.info("Executed place block create action via ChatBot for planId: {}", planId);
                    break;

                case "update":
                    var updateResponse = sharedTimeTablePlaceBlockService.update(request);
                    redisSyncService.publish(
                        "/topic/" + planId,
                        updateResponse
                    );
                    log.info("Executed place block update action via ChatBot for planId: {}", planId);
                    break;

                case "delete":
                    var deleteResponse = sharedTimeTablePlaceBlockService.delete(request);
                    redisSyncService.publish(
                        "/topic/" + planId,
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
