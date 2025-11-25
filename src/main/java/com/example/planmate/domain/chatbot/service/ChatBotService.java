package com.example.planmate.domain.chatbot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.planmate.domain.plan.entity.Plan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    @Value("${python.chatbot.api.url:http://localhost:8010/api/chatbot/generate}")
    private String pythonApiUrl;

    public ChatBotActionResponse getChatResponse(String message, Integer planId, String planContext) {
        try {

            String systemPromptContext = buildSystemPromptContext(planId);

            // planContext를 Map으로 구성 (Python이 dict를 기대)
            Map<String, Object> planContextMap = buildPlanContextMap(planId);

            // Python 서버로 전송할 요청 본문 구성
            Map<String, Object> requestBody = Map.of(
                    "planId", planId,
                    "message", message,
                    "systemPromptContext", systemPromptContext,
                    "planContext", planContextMap
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("요청을 Python 챗봇 서버로 전달: {}", pythonApiUrl);

            ResponseEntity<ChatBotActionResponse> response = restTemplate.exchange(
                    pythonApiUrl,
                    HttpMethod.POST,
                    entity,
                    ChatBotActionResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ChatBotActionResponse pythonResponse = response.getBody();
                log.info("Successfully received ChatBotActionResponse from Python server.");

                // Python 서버에서 Action이 실행되어야 한다고 판단한 경우, Java 서버에서 Action 실행
                if (pythonResponse.isHasAction() && pythonResponse.getActions() != null && !pythonResponse.getActions().isEmpty()) {
                    return executeActions(pythonResponse.getActions(), planId, pythonResponse.getUserMessage());
                } else {
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

    private ChatBotActionResponse executeActions(List<ChatBotActionResponse.ActionData> actions, Integer planId, String originalUserMessage) {
        StringBuilder combinedMessage = new StringBuilder();
        if (originalUserMessage != null && !originalUserMessage.isBlank()) {
            combinedMessage.append(originalUserMessage.trim());
        }

        List<ChatBotActionResponse.ActionData> aggregatedActions = new ArrayList<>();

        for (ChatBotActionResponse.ActionData actionData : actions) {
            ChatBotActionResponse actionResult = executeAction(actionData, planId);
            if (actionResult == null) {
                log.warn("No action result returned for action: {} target: {}", actionData.getAction(), actionData.getTargetName());
                continue;
            }

            if (actionResult.getUserMessage() != null && !actionResult.getUserMessage().isBlank()) {
                if (combinedMessage.length() > 0) {
                    combinedMessage.append("\n");
                }
                combinedMessage.append(actionResult.getUserMessage().trim());
            }

            if (actionResult.isHasAction() && actionResult.getActions() != null && !actionResult.getActions().isEmpty()) {
                aggregatedActions.addAll(actionResult.getActions());
            }
        }

        String finalMessage = combinedMessage.length() > 0
                ? combinedMessage.toString()
                : (originalUserMessage != null ? originalUserMessage : "");

        if (aggregatedActions.isEmpty()) {
            return ChatBotActionResponse.simpleMessage(finalMessage);
        }

        return new ChatBotActionResponse(finalMessage, true, aggregatedActions);
    }

    private ChatBotActionResponse executeAction(ChatBotActionResponse.ActionData actionData, Integer planId) {
        if (actionData == null) {
            return null;
        }

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
                default:
                    log.warn("Unsupported action target received from Python: {}", targetName);
                    actionResult = ChatBotActionResponse.simpleMessage("지원하지 않는 작업 대상입니다: " + targetName);
                    break;
            }

            return actionResult;

        } catch (Exception e) {
            log.error("Error executing action received from Python: {}", e.getMessage(), e);
            return ChatBotActionResponse.simpleMessage("액션 실행 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private Map<String, Object> buildPlanContextMap(Integer planId) {
        try {
            Plan plan = redisService.findPlanByPlanId(planId);
            if (plan == null) {
                log.error("Plan not found in Redis: planId={}", planId);
                throw new IllegalStateException("Plan not found in Redis: " + planId);
            }

            PlanDto planDto = PlanDto.fromEntity(plan);
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

            // Python의 planContext dict 형식으로 반환
            Map<String, Object> planContextMap = new java.util.HashMap<>();
            planContextMap.put("Plan", planDto);
            planContextMap.put("TimeTables", timeTables);
            planContextMap.put("TimeTablePlaceBlocks", timeTablePlaceBlocks);

            return planContextMap;
        } catch (Exception e) {
            log.error("Error building plan context map: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to build plan context: " + e.getMessage(), e);
        }
    }

    private String buildSystemPromptContext(Integer planId) throws JsonProcessingException {
        PlanDto planDto = PlanDto.fromEntity(redisService.findPlanByPlanId(planId)); //문제가 있다면 여기
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
            당신은 여행 계획 도우미 AI입니다. 사용 가능한 도구(Tools)를 사용하여 사용자의 요청을 처리하세요.

            ### 현재 계획 데이터
            Plan: %s
            TimeTables: %s
            TimeTablePlaceBlocks: %s

            ### 역할 및 사용 가능한 기능
            1. **장소 검색 및 추가**: 사용자가 장소를 추가하고 싶을 때 search_multiple_place_blocks 또는 search_and_create_place_block 함수를 호출하세요.
               - 예: "명동 맛집 3곳 추가해줘" → search_multiple_place_blocks(queries=["명동 맛집", "명동 맛집", "명동 맛집"], timeTableId=...)
               - 예: "경복궁 추가해줘" → search_and_create_place_block(query="경복궁", timeTableId=...)

            2. **일정 수정/삭제**: 사용자가 기존 일정을 수정하거나 삭제하고 싶을 때 반드시 JSON 응답을 반환하세요.
               - 시간 변경, 제목 변경, 일정 삭제 등
               - "점심"은 11:00~14:00, "저녁"은 17:00~20:00 시간대를 의미합니다.
               - 사용자가 "점심에 일정 삭제"라고 하면 해당 시간대의 블록을 찾아서 삭제하세요.

            ### 중요 규칙
            1. **장소 추가 요청 시**: 반드시 함수를 호출하세요. JSON으로 응답하지 마세요.
            2. **일정 수정/삭제 시**: 반드시 JSON 형식으로만 응답하세요. 일반 텍스트로 응답하지 마세요.
            3. **시간 겹침 금지**: 같은 timeTableId 내에서 blockStartTime~blockEndTime이 겹치면 안 됩니다.
            4. **timeTableId 찾기**: TimeTables에서 사용자가 언급한 날짜("1일차", "2일차")에 해당하는 timeTableId를 찾으세요.
            5. **시간대 해석**:
               - "아침": 06:00~10:00
               - "점심": 11:00~14:00
               - "오후": 14:00~18:00
               - "저녁": 17:00~20:00

            ### JSON 응답 형식 (수정/삭제 시 필수)
            반드시 아래 JSON 형식으로만 응답하세요. JSON 외의 텍스트는 절대 포함하지 마세요.

            {
              "userMessage": "친근한 한국어 메시지",
              "hasAction": true/false,
              "actions": [
                {
                  "action": "create | update | delete",
                  "targetName": "plan | timeTable | timeTablePlaceBlock",
                  "target": { ... }
                }
              ]
            }

            ### 예시
            - "1일차 점심에 명동 맛집 3곳 추가해줘" → search_multiple_place_blocks 함수 호출
            - "경복궁 일정에 추가해줘" → search_and_create_place_block 함수 호출
            - "1일차 점심에 일정 삭제해줘" → JSON 응답 (delete, 점심 시간대(11:00~14:00)의 블록 찾아서 삭제)
            - "경복궁 삭제해줘" → JSON 응답 (delete, blockId=20)
            - "시작 시간 1시간 뒤로 미뤄줘" → JSON 응답 (update)

            ### 최종 지시
            - 장소 검색 요청이면: 함수 호출
            - 수정/삭제 요청이면: 반드시 JSON만 반환
            - 사용자에게 다시 물어보지 마세요. 현재 데이터를 기반으로 최선의 판단을 내리고 바로 실행하세요.
            - 예: "점심에 일정 삭제"라고 하면, 점심 시간대(11:00~14:00)와 겹치는 블록을 찾아서 바로 삭제하세요.
            """.formatted(planJson, timeTablesJson, timeTablePlaceBlocksJson);
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
                    String date = (String)timeTableMap.get("date");
                    if(date != null){
                        return chatBotPlanService.createTimeTable(planId, timeTableJson);
                    }
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
                    Integer placeBlockId = (Integer) placeBlockMap.get("blockId");
                    if (placeBlockId != null) {
                        return chatBotPlanService.updateTimeTablePlaceBlock(placeBlockId, placeBlockJson);
                    }
                    break;
                case "delete":
                    Integer deletePlaceBlockId = (Integer) placeBlockMap.get("blockId");
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