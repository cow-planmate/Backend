package com.example.planmate.domain.chatbot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.planmate.domain.chatbot.dto.ActionData;
import com.example.planmate.domain.chatbot.dto.ChatBotActionResponse;
import com.example.planmate.domain.webSocket.lazydto.PlanDto;
import com.example.planmate.domain.webSocket.lazydto.TimeTableDto;
import com.example.planmate.domain.webSocket.lazydto.TimeTablePlaceBlockDto;
import com.example.planmate.domain.webSocket.service.RedisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatBotService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ChatBotPlanService chatBotPlanService;
    private final RedisService redisService;

    @Value("${python.chatbot.api.url:http://localhost:5000/api/chatbot/generate}")
    private String pythonApiUrl;
    
    public ChatBotActionResponse getChatResponse(String message, Integer planId, String planContext) {
        try {

            String systemPromptContext = buildSystemPromptContext(planId);

            // 2. Python 서버로 전송할 요청 본문 구성
            Map<String, Object> requestBody = Map.of(
                    "planId", planId,
                    "message", message,
                    "systemPromptContext", systemPromptContext,
                    "planContext", planContext != null ? planContext : ""
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("요청을 Python 챗봇 서버로 전달: {}", pythonApiUrl);

            ResponseEntity<ChatBotActionResponse> response = restTemplate.exchange(
                    pythonApiUrl,
                    HttpMethod.POST,
                    entity,
                    ChatBotActionResponse.class // Python 응답을 직접 ChatBotActionResponse 객체로 받음
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ChatBotActionResponse pythonResponse = response.getBody();
                log.info("Successfully received ChatBotActionResponse from Python server.");

                // 4. Python 서버에서 Action이 실행되어야 한다고 판단한 경우, Java 서버에서 Action 실행
                if (pythonResponse.isHasAction() && pythonResponse.getActions() != null) {
                    List<ChatBotActionResponse.ActionData> actions = pythonResponse.getActions();
                    ChatBotActionResponse actionResult = new ChatBotActionResponse();
                    
                    for (ChatBotActionResponse.ActionData actionData : actions) {
                        actionResult.addAction(actionData);
                        // 필요시 actionResult를 활용하여 추가 처리 가능
                    }
                    return executeAction(pythonResponse.getActions(), planId, pythonResponse.getUserMessage());
                } else {
                    // Action이 없는 경우, Python이 생성한 단순 메시지 반환
                    return pythonResponse;
                }
            } else {
                log.error("Python API call failed with status: {}", response.getStatusCode());
                return ChatBotActionResponse.simpleMessage("죄송합니다. AI 챗봇 서비스 연결에 문제가 발생했습니다.");
            }

        } catch (Exception e) {
            log.error("Error in getChatResponse when communicating with Python server: {}", e.getMessage());
            return ChatBotActionResponse.simpleMessage("죄송합니다. 현재 AI 챗봇 서비스에 문제가 있습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    private ActionData executeAction(ChatBotActionResponse.ActionData actionData, Integer planId, String originalUserMessage) {
        try {
            String action = actionData.getAction();
            String targetName = actionData.getTargetName();
            Object target = actionData.getTarget();

            ChatBotActionResponse actionResult = null;

            switch (targetName) {
                case "plan":
                    actionResult = executePlanAction(action, target, planId);
                    break;

                case "timeTable":
                    actionResult = executeTimeTableAction(action, target, planId);
                    break;

                case "timeTablePlaceBlock":
                    actionResult = executeTimeTablePlaceBlockAction(action, target, planId);
                    break;
            }

            if (actionResult != null && actionResult.isHasAction()) {
                // Python이 생성한 메시지와 Java에서 실행 결과 생성된 메시지를 결합
                String combinedMessage = originalUserMessage;
                if (actionResult.getUserMessage() != null && !actionResult.getUserMessage().isEmpty()) {
                    combinedMessage += "\n" + actionResult.getUserMessage();
                }
                return new ChatBotActionResponse(combinedMessage, true, actionResult.getAction());
            }

            // 액션 실행은 했으나 액션 반환값에 문제가 있거나, 실행 후 Action이 없는 경우 원본 메시지만 반환
            return ChatBotActionResponse.simpleMessage(originalUserMessage);

        } catch (Exception e) {
            log.error("Error executing action received from Python: {}", e.getMessage());
            return ChatBotActionResponse.simpleMessage("액션 실행 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private String buildSystemPromptContext(Integer planId) throws JsonProcessingException {
        PlanDto planDto = PlanDto.fromEntity(redisService.findPlanByPlanId(planId));
        List<TimeTableDto> timeTables = redisService.findTimeTablesByPlanId(planId)
                .stream()
                .map(TimeTableDto::fromEntity)
                .toList();

        List<TimeTablePlaceBlockDto> timeTablePlaceBlocks = new ArrayList<>();
        for (TimeTableDto timeTable : timeTables) {
            List<TimeTablePlaceBlockDto> blocks = redisService.findTimeTablePlaceBlocksByTimeTableId(timeTable.timeTableId())
                    .stream()
                    .map(TimeTablePlaceBlockDto::fromEntity)
                    .toList();
            timeTablePlaceBlocks.addAll(blocks);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        String planJson = objectMapper.writeValueAsString(planDto);
        String timeTablesJson = objectMapper.writeValueAsString(timeTables);
        String timeTablePlaceBlocksJson = objectMapper.writeValueAsString(timeTablePlaceBlocks);

        // Python 서버의 AI 모델에 전달할 컨텍스트 데이터
        return """
                당신은 여행 계획 도우미 AI입니다.
                사용자의 여행 계획을 도와주고, 필요시 계획을 수정하거나 제안할 수 있습니다.
                
                ---
                ### 🔹 역할
                - 사용자의 여행 계획 데이터를 분석하고, 필요시 수정 제안을 합니다.
                - 사용자의 요청에 따라 계획, 타임테이블, 또는 장소 블록을 생성/수정/삭제할 수 있습니다.
                - 하루 또는 일정 기간의 여행 계획을 최적화하고 개선하는 데 도움을 줍니다.
                - 사용자의 일정과 장소를 기반으로 여행 비용을 추정할 수 있습니다.
                
                ---
                ### 🔹 입력 데이터 (JSON)
                다음은 사용자의 여행 계획 데이터입니다.
                
                Plan:
                %s
                
                TimeTables:
                %s
                
                TimeTablePlaceBlocks:
                %s
                
                ---

                ---
                ### 🔹 학습할 내용
                - Plan, TimeTable, TimeTablePlaceBlock의 JSON 구조와 필드를 이해합니다.
                - 각 엔티티 간의 관계와 종속성을 파악합니다.
                - 여행 계획의 논리적 흐름과 시간적 제약 조건을 이해합니다.
                ---

                ### 🔹 응답 형식 (ChatBotActionResponse)
                AI의 응답은 반드시 아래 형식을 따라야 합니다.
                **중요** 반드시 JSON으로 반환을 해야 합니다.
                delete를 제외하고는 target의 모든 값을 다 반환해야 합니다.
                timeTablePlaceBlock은 생성하거나 수정할 때 같은 timeTable안에 있는 다른 timeTablePlaceBlock과 시간이 겹치면 안됩니다.
                {
                  "userMessage": "사용자에게 보여줄 친근한 메시지",
                  "hasAction": true or false,
                  "actions": {
                    {
                        "action": "create | update | delete",
                        "targetName": "plan | timeTable | timeTablePlaceBlock",
                        "target": { ... } // 실제 JSON 데이터
                    }
                    {
                        "action": "create | update | delete",
                        "targetName": "plan | timeTable | timeTablePlaceBlock",
                        "target": { ... } // 실제 JSON 데이터
                    }
                  }
                }""".formatted(planJson, timeTablesJson, timeTablePlaceBlocksJson);
    }

    private ChatBotActionResponse executePlanAction(String action, Object target, int planId) {
        try {
            if ("update".equals(action)) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());

                // 전체 plan 객체를 한 번에 처리
                @SuppressWarnings("unchecked")
                Map<String, Object> planMap = (Map<String, Object>) target;

                // Map을 JSON 문자열로 변환 후 다시 객체로 파싱하여 전체 업데이트
                String planJson = objectMapper.writeValueAsString(planMap);

                // 전체 plan 데이터를 ChatBotPlanService로 전달하여 업데이트
                return chatBotPlanService.updateFullPlan(planId, planJson);
            }
            return null;
        } catch (Exception e) {
            log.error("Error executing plan action: {}", e.getMessage());
            return null;
        }
    }
    
    private ChatBotActionResponse executeTimeTableAction(String action, Object target, int planId) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            @SuppressWarnings("unchecked")
            Map<String, Object> timeTableMap = (Map<String, Object>) target;
            String timeTableJson = objectMapper.writeValueAsString(timeTableMap);

            switch (action) {
                case "create":
                    return chatBotPlanService.createTimeTable(planId, timeTableJson);
                case "update":
                    Integer timeTableId = (Integer) timeTableMap.get("timeTableId");
                    if (timeTableId != null) {
                        return chatBotPlanService.updateTimeTable(timeTableId, timeTableJson);
                    }
                    break;
                case "delete":
                    Integer deleteTimeTableId = (Integer) timeTableMap.get("timeTableId");
                    if (deleteTimeTableId != null) {
                        return chatBotPlanService.deleteTimeTable(deleteTimeTableId);
                    }
                    break;
            }
            return null;
        } catch (Exception e) {
            log.error("Error executing timeTable action: {}", e.getMessage());
            return null;
        }
    }
    
    private ChatBotActionResponse executeTimeTablePlaceBlockAction(String action, Object target, int planId) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            
            @SuppressWarnings("unchecked")
            Map<String, Object> placeBlockMap = (Map<String, Object>) target;
            String placeBlockJson = objectMapper.writeValueAsString(placeBlockMap);
            
            switch (action) {
                case "create":
                    Integer timeTableId = (Integer) placeBlockMap.get("timeTableId");
                    if (timeTableId != null) {
                        return chatBotPlanService.createTimeTablePlaceBlock(timeTableId, placeBlockJson);
                    }
                    break;
                case "update":
                    Integer placeBlockId = (Integer) placeBlockMap.get("timeTablePlaceBlockId");
                    if (placeBlockId != null) {
                        return chatBotPlanService.updateTimeTablePlaceBlock(placeBlockId, placeBlockJson);
                    }
                    break;
                case "delete":
                    Integer deletePlaceBlockId = (Integer) placeBlockMap.get("timeTablePlaceBlockId");
                    if (deletePlaceBlockId != null) {
                        return chatBotPlanService.deleteTimeTablePlaceBlock(deletePlaceBlockId);
                    }
                    break;
            }
            return null;
        } catch (Exception e) {
            log.error("Error executing timeTablePlaceBlock action: {}", e.getMessage());
            return null;
        }
    }
}