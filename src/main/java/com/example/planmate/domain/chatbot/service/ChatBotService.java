package com.example.planmate.domain.chatbot.service;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatBotService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ChatBotPlanService chatBotPlanService;
    private final RedisService redisService;

    @Value("${python.chatbot.api.url:http://localhost:8010/api/chatbot/generate}")
    private String pythonApiUrl;
    
    public ChatBotActionResponse getChatResponse(String message, Integer planId) {
        try {

            String systemPromptContext = buildSystemPromptContext(planId);

            // planContext를 객체 형태로 생성
            Map<String, Object> planContextMap = buildPlanContextMap(planId);

            // 2. Python 서버로 전송할 요청 본문 구성
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
                    ChatBotActionResponse.class // Python 응답을 직접 ChatBotActionResponse 객체로 받음
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ChatBotActionResponse pythonResponse = response.getBody();
                log.info("Successfully received ChatBotActionResponse from Python server.");

                // 4. Python 서버에서 Action이 실행되어야 한다고 판단한 경우, Java 서버에서 Action 실행
                if (pythonResponse.isHasAction() && pythonResponse.getActions() != null && !pythonResponse.getActions().isEmpty()) {
                    return executeActions(pythonResponse.getActions(), planId, pythonResponse.getUserMessage());
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

    private ChatBotActionResponse executeActions(List<ChatBotActionResponse.ActionData> actions, Integer planId, String originalUserMessage) {
        List<ChatBotActionResponse.ActionData> aggregatedActions = new ArrayList<>();
        StringBuilder errorMessages = new StringBuilder();

        // 날짜 → timeTableId 매핑용 Map
        java.util.Map<String, Integer> dateToTimeTableIdMap = new java.util.HashMap<>();

        // 1단계: timeTable 생성 액션 먼저 실행 (Redis에 직접 저장)
        for (ChatBotActionResponse.ActionData actionData : actions) {
            if ("timeTable".equals(actionData.getTargetName()) && "create".equals(actionData.getAction())) {
                try {
                    // target에서 날짜 추출
                    Object target = actionData.getTarget();
                    if (!(target instanceof java.util.Map)) {
                        log.warn("TimeTable target is not a Map");
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> targetMap = (java.util.Map<String, Object>) target;
                    String dateStr = (String) targetMap.get("date");

                    if (dateStr == null) {
                        log.warn("TimeTable target has no date");
                        continue;
                    }

                    java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
                    java.time.LocalTime startTime = java.time.LocalTime.of(9, 0);
                    java.time.LocalTime endTime = java.time.LocalTime.of(20, 0);

                    // Plan 조회
                    var plan = redisService.findPlanByPlanId(planId);

                    // TimeTable 엔티티 생성
                    var timeTable = com.example.planmate.domain.plan.entity.TimeTable.builder()
                        .plan(plan)
                        .date(date)
                        .timeTableStartTime(startTime)
                        .timeTableEndTime(endTime)
                        .build();

                    // Redis에 직접 저장
                    int createdId = redisService.getNewTimeTableId(timeTable);

                    log.info("Created TimeTable with ID {} for date {}", createdId, date);

                    // 날짜 → ID 매핑 저장
                    dateToTimeTableIdMap.put(dateStr, createdId);

                    // 응답용 액션 데이터 생성
                    var responseVO = new com.example.planmate.common.valueObject.TimetableVO();
                    responseVO.setTimetableId(createdId);
                    responseVO.setDate(date);
                    responseVO.setStartTime(startTime);
                    responseVO.setEndTime(endTime);

                    var request = new com.example.planmate.domain.webSocket.dto.WTimetableRequest();
                    request.setTimetableVOs(java.util.List.of(responseVO));

                    var responseAction = new ChatBotActionResponse.ActionData("create", "timeTable", request);
                    aggregatedActions.add(responseAction);

                } catch (Exception e) {
                    log.error("Failed to create TimeTable: {}", e.getMessage(), e);
                    if (errorMessages.length() > 0) {
                        errorMessages.append("\n");
                    }
                    errorMessages.append("타임테이블 생성 실패: ").append(e.getMessage());
                }
            }
        }

        // 2단계: PlaceBlock 액션 실행 (음수 timeTableId를 날짜로 매핑)
        for (ChatBotActionResponse.ActionData actionData : actions) {
            // timeTable create는 이미 실행했으므로 건너뜀
            if ("timeTable".equals(actionData.getTargetName()) && "create".equals(actionData.getAction())) {
                continue;
            }

            // PlaceBlock의 음수 timeTableId를 실제 ID로 교체
            if ("timeTablePlaceBlock".equals(actionData.getTargetName())) {
                Object target = actionData.getTarget();
                if (target instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> targetMap = (java.util.Map<String, Object>) target;

                    Object timeTableIdObj = targetMap.get("timeTableId");
                    String dateStr = (String) targetMap.get("date");

                    // timeTableId가 음수면 날짜로 매핑
                    if (timeTableIdObj instanceof Integer) {
                        int timeTableId = (Integer) timeTableIdObj;
                        if (timeTableId < 0 && dateStr != null) {
                            Integer realId = dateToTimeTableIdMap.get(dateStr);
                            if (realId != null) {
                                targetMap.put("timeTableId", realId);
                                log.info("Mapped PlaceBlock date {} to timeTableId {} (was {})", dateStr, realId, timeTableId);
                            } else {
                                log.error("날짜 {}에 해당하는 TimeTable을 찾을 수 없습니다.", dateStr);
                                if (errorMessages.length() > 0) {
                                    errorMessages.append("\n");
                                }
                                errorMessages.append("날짜 ").append(dateStr).append("에 해당하는 TimeTable을 찾을 수 없습니다.");
                                continue;
                            }
                        }
                    }
                }
            }

            ChatBotActionResponse actionResult = executeAction(actionData, planId);
            if (actionResult == null) {
                log.warn("No action result returned for action: {} target: {}", actionData.getAction(), actionData.getTargetName());
                continue;
            }

            // 에러 메시지만 수집 (성공 메시지는 무시)
            if (actionResult.getUserMessage() != null && !actionResult.getUserMessage().isBlank()) {
                String message = actionResult.getUserMessage().trim();
                // 에러/실패 메시지만 추가 (성공 메시지는 제외)
                if (message.contains("실패") || message.contains("오류") || message.contains("에러") || message.contains("Error")) {
                    if (errorMessages.length() > 0) {
                        errorMessages.append("\n");
                    }
                    errorMessages.append(message);
                }
            }

            if (actionResult.isHasAction() && actionResult.getActions() != null && !actionResult.getActions().isEmpty()) {
                aggregatedActions.addAll(actionResult.getActions());
            }
        }

        // 에러가 있으면 원본 메시지에 에러 추가, 없으면 원본 메시지만 사용
        String finalMessage;
        if (errorMessages.length() > 0) {
            finalMessage = (originalUserMessage != null && !originalUserMessage.isBlank() ? originalUserMessage + "\n\n" : "")
                          + "⚠️ 일부 작업 중 오류가 발생했습니다:\n" + errorMessages.toString();
        } else {
            finalMessage = originalUserMessage != null && !originalUserMessage.isBlank()
                          ? originalUserMessage
                          : "";
        }

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

        // 일차별 timeTableId 매핑 생성
        StringBuilder dayMapping = new StringBuilder();
        for (int i = 0; i < timeTables.size(); i++) {
            TimeTableDto tt = timeTables.get(i);
            dayMapping.append(String.format("%d일차: timeTableId=%d (date=%s)\n",
                i + 1, tt.timeTableId(), tt.date()));
        }
        String dayMappingStr = dayMapping.toString();

        return """
            당신은 여행 계획 도우미 AI입니다. 사용 가능한 도구(Tools)를 사용하여 사용자의 요청을 처리하세요.

            ### 현재 계획 데이터
            Plan: %s
            TimeTables: %s
            TimeTablePlaceBlocks: %s

            ### 일차별 timeTableId 매핑
            %s

            ### 역할 및 사용 가능한 기능
            1. **장소 검색 및 추가**: 사용자가 장소를 추가하고 싶을 때 search_multiple_place_blocks 또는 search_and_create_place_block 함수를 호출하세요.
               - 예: "명동 맛집 3곳 추가해줘" → search_multiple_place_blocks(queries=["명동 맛집", "명동 맛집", "명동 맛집"], timeTableId=...)
               - 예: "경복궁 추가해줘" → search_and_create_place_block(query="경복궁", timeTableId=...)
               - 예: "2일차에 남산타워 추가해줘" → 위의 일차별 매핑에서 2일차의 timeTableId를 찾아서 사용

            2. **일차(TimeTable) 자동 생성**: 사용자가 요청한 일차가 아직 존재하지 않으면 자동으로 생성하세요.
               - 예: 현재 2일차까지만 있는데 "5일차 만들어줘" → 3일차, 4일차, 5일차 TimeTable을 생성
               - 새 일차의 날짜는 마지막 일차의 날짜에서 순차적으로 +1일씩 계산하세요
               - **중요**: 존재하지 않는 일차에 장소 추가 요청이 있으면, **일차만 먼저 생성**하고 **장소 추가는 사용자에게 다시 요청하라고 안내**하세요
               - 예: "4일차 점심에 회 맛집 추가해줘" (현재 2일차까지만 있음)
                 → 3일차, 4일차만 생성하고, 사용자에게 "3일차, 4일차를 생성했어요! 이제 다시 '4일차 점심에 회 맛집 추가해줘'라고 말씀해주세요."라고 안내
               - JSON 응답 형식:
                 {
                   "userMessage": "3일차, 4일차를 생성했어요! 이제 다시 장소 추가를 요청해주세요.",
                   "hasAction": true,
                   "actions": [
                     {
                       "action": "create",
                       "targetName": "timeTable",
                       "target": {"date": "2025-01-03"}
                     },
                     {
                       "action": "create",
                       "targetName": "timeTable",
                       "target": {"date": "2025-01-04"}
                     }
                   ]
                 }

            3. **일정 수정/삭제**: 사용자가 기존 일정을 수정하거나 삭제하고 싶을 때 반드시 JSON 응답을 반환하세요.
               - 시간 변경, 제목 변경, 일정 삭제 등
               - "점심"은 11:00~14:00, "저녁"은 17:00~20:00 시간대를 의미합니다.
               - 사용자가 "점심에 일정 삭제"라고 하면 해당 시간대의 블록을 찾아서 삭제하세요.

            ### 중요 규칙
            1. **장소 추가 요청 시**: 반드시 함수를 호출하세요. JSON으로 응답하지 마세요.
            2. **일차 생성 시**: 반드시 JSON 형식으로 응답하세요.
            3. **일정 수정/삭제 시**: 반드시 JSON 형식으로만 응답하세요. 일반 텍스트로 응답하지 마세요.
            4. **시간 겹침 금지**: 같은 timeTableId 내에서 blockStartTime~blockEndTime이 겹치면 안 됩니다.
            5. **timeTableId 찾기**: 위의 '일차별 timeTableId 매핑' 정보를 참고하여 사용자가 언급한 날짜("1일차", "2일차" 등)에 해당하는 timeTableId를 정확히 찾으세요.
            6. **존재하지 않는 일차 처리**: 사용자가 요청한 일차가 현재 매핑에 없으면, 먼저 해당 일차까지 TimeTable을 생성하세요.
            7. **시간대 해석**:
               - "아침": 06:00~10:00
               - "점심": 11:00~14:00
               - "오후": 14:00~18:00
               - "저녁": 17:00~20:00
            8. **엔티티 필드 구조**:
               - TimeTablePlaceBlock 필드: blockId, placeName, placeTheme, placeRating, placeAddress, placeLink,
                 blockStartTime, blockEndTime, xLocation, yLocation, placeId, placeCategoryId, timeTableId
               - placeCategoryId: 0(관광지), 1(숙소), 2(식당)만 사용
               - 필드를 임의로 추가/제거하지 말고 입력 JSON 구조를 그대로 유지

            ### JSON 응답 형식 (수정/삭제/일차 생성 시 필수)
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
            - "1일차 점심에 명동 맛집 3곳 추가해줘" (1일차 존재함) → search_multiple_place_blocks 함수 호출 (1일차의 timeTableId 사용)
            - "경복궁 일정에 추가해줘" (기존 일차 존재) → search_and_create_place_block 함수 호출
            - "5일차 만들어줘" (현재 2일차까지만 있음) → JSON 응답으로 3일차, 4일차, 5일차 create (날짜는 마지막 날짜 +1일, +2일, +3일)
            - **"4일차 점심에 회 맛집 추가해줘" (현재 2일차까지만 있음)** → JSON 응답으로 3일차, 4일차만 create. userMessage에 "3일차, 4일차를 생성했어요! 이제 다시 장소 추가를 요청해주세요."
            - "1일차 점심에 일정 삭제해줘" → JSON 응답 (delete, 점심 시간대(11:00~14:00)의 블록 찾아서 삭제)
            - "경복궁 삭제해줘" → JSON 응답 (delete, 해당 blockId 찾아서 삭제)
            - "시작 시간 1시간 뒤로 미뤄줘" → JSON 응답 (update)

            ### 최종 지시
            - 장소 검색 요청이면: 함수 호출
            - 수정/삭제/일차 생성 요청이면: 반드시 JSON만 반환
            - 사용자에게 다시 물어보지 마세요. 현재 데이터를 기반으로 최선의 판단을 내리고 바로 실행하세요.
            - 예: "점심에 일정 삭제"라고 하면, 점심 시간대(11:00~14:00)와 겹치는 블록을 찾아서 바로 삭제하세요.
            """.formatted(planJson, timeTablesJson, timeTablePlaceBlocksJson, dayMappingStr);
    }

    private Map<String, Object> buildPlanContextMap(Integer planId) {
        try {
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

            // Map 형태로 반환
            Map<String, Object> contextMap = new java.util.HashMap<>();
            contextMap.put("TravelName", planDto.planName() != null ? planDto.planName() : "");
            contextMap.put("PlanId", planDto.planId());
            contextMap.put("TimeTables", timeTables);
            contextMap.put("TimeTablePlaceBlocks", timeTablePlaceBlocks);

            return contextMap;

        } catch (Exception e) {
            log.error("Error building plan context map: {}", e.getMessage());
            return Map.of(); // 빈 맵 반환
        }
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