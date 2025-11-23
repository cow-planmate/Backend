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
            Object targetObj = actionData.getTarget();
            Object target = targetObj;

            String json = null;

            // 1. 데이터 추출
            if (targetObj instanceof Map<?,?> map && map.containsKey("raw_string_data")) {
                json = (String) map.get("raw_string_data");
            } else if (targetObj instanceof String str && str.startsWith("raw_string_data=")) {
                json = str.replace("raw_string_data=", "");
            }

            // 2. JSON 문자열 보정 (앞뒤 괄호/따옴표 강제 주입)
            if (json != null) {
                json = json.trim(); // 공백 제거

                // (1) 시작 부분 보정: 'blockId' 처럼 시작하면 '{"blockId' 로 변경
                if (!json.startsWith("{")) {
                    json = "{\"" + json;
                }

                // (2) 끝 부분 보정: '}'로 끝나지 않으면 '}' 추가
                if (!json.endsWith("}")) {
                    json = json + "}";
                }

                // 3. 파싱 시도
                try {
                    System.out.println("보정된 JSON: " + json); // 디버깅용 로그
                    ObjectMapper objectMapper = new ObjectMapper();
                    target = objectMapper.readValue(json, Map.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    // 에러 발생 시 원본 문자열 확인 필요
                }
            }

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

        // Python 서버의 AI 모델에 전달할 컨텍스트 데이터
        return """
            당신은 여행 계획 도우미 AI이다.
            사용자의 여행 계획을 도와주고, 필요시 계획을 수정하거나 제안할 수 있다.

            ---
            ### 🔹 역할
            - 사용자의 여행 계획 데이터를 분석하고, 상황에 맞는 수정 제안을 한다.
            - 사용자의 요청에 따라 Plan, TimeTable, TimeTablePlaceBlock을 생성(create)·수정(update)·삭제(delete)한다.

            ---
            ### 🔹 입력 데이터 (JSON)
            다음은 사용자의 현재 여행 계획 데이터이다.

            Plan:
            %s

            TimeTables:
            %s

            TimeTablePlaceBlocks:
            %s

            위 JSON들은 실제 서비스에서 사용하는 원본 구조이며,
            AI는 **반드시 이 구조를 그대로 이해하고, 동일한 구조로 응답을 생성해야 한다.**

            ---
            ### 🔹 엔티티 구조 설명 (특히 TimeTablePlaceBlock)

            1. Plan
            - 여행 전체 단위의 메타 정보이다.
            - 실제 필드명과 구조는 Plan JSON에 나와 있는 것을 그대로 따른다.

            2. TimeTable
            - 특정 날짜(하루) 단위의 일정이다.
            - 실제 필드명과 구조는 TimeTables JSON에 나와 있는 것을 그대로 따른다.

            3. TimeTablePlaceBlock 
            새로운 블록을 생성(create)할 때는 아래 필드를 **빠짐없이** 채워라.
    
            - **blockId**: 0 (새로 생성 시 0으로 고정)
            - **placeName**: 장소명 (사용자 요청에 따름)
            - **placeTheme**: 테마 (예: '맛집', '산책', '쇼핑' 등 AI가 판단하여 기입)
            - **placeRating**: 0.0 ~ 5.0 사이 (모르면 4.0으로 기입)
            - **placeAddress**: 주소 (모르면 '주소 정보 없음' 또는 시/군/구 단위까지만이라도 기입)
            - **placeLink**: (모르면 빈 문자열 "")
            - **blockStartTime**: "HH:mm:ss"
            - **blockEndTime**: "HH:mm:ss"
            - **xLocation**: 위도 (정확히 모르면 해당 지역의 대략적인 위도라도 기입. **0.0 금지**)
            - **yLocation**: 경도 (정확히 모르면 해당 지역의 대략적인 경도라도 기입. **0.0 금지**)
            - **placeId**: (모르면 빈 문자열 "")
            - **placeCategoryId**: 0(관광), 1(숙소), 2(식당) 중 택 1
            - **timeTableId**: 연결될 TimeTable의 ID

            ---
            ### 🔹 시간 겹침 제약 조건
            - 같은 timeTableId 내에서 blockStartTime ~ blockEndTime 구간이 겹치지 않도록 한다.

            ---
            ### 🔹 응답 형식 (ChatBotActionResponse)

            AI의 응답은 **반드시 아래 JSON 형식만** 반환해야 한다.  
            JSON 외의 텍스트는 절대 포함하지 않는다.

            {
              "userMessage": "사용자에게 보여줄 메시지",
              "hasAction": true,
              "actions": [
                {
                  "action": "create | update | delete",
                  "targetName": "plan | timeTable | timeTablePlaceBlock",
                  "target": { ...객체 전체 데이터... }
                }
              ]
            }

            #### ⚠️ 필수 검증 규칙 (반드시 준수)

            1. **Create 액션의 Target 데이터 강제**
               - action이 "create"일 경우, `target` 필드는 **절대 비어있거나 `{}`이면 안 된다.**
               - AI는 사용자가 언급한 장소의 정보(좌표, 주소, 테마 등)를 **스스로 찾거나 추론하여** `target` 객체의 모든 필드를 완벽하게 채워야 한다.
               - 클라이언트가 정보를 채워줄 것이라고 가정하지 말고, **AI가 완성된 데이터를 내려줘야 한다.**

            2. **Target 객체 구조 유지**
               - `target`에는 위에서 설명한 엔티티의 모든 필드가 포함되어야 한다. (delete 제외)
               - 필드명을 생략하거나 변경하지 말 것.

            3. **Delete 액션**
               - `target`에 식별자(ID)만 포함해도 된다.

            ---
            ### 🔹 동작 예시 (create 시 Target이 꽉 차 있는 예시)

            {
              "userMessage": "네, 21일 점심에 '명동교자' 일정을 추가했습니다.",
              "hasAction": true,
              "actions": [
                {
                  "action": "create",
                  "targetName": "timeTablePlaceBlock",
                  "target": {
                    "blockId": 1005,
                    "placeName": "명동교자 본점",
                    "placeTheme": "맛집",
                    "placeRating": 4.5,
                    "placeAddress": "서울 중구 명동10길 29",
                    "placeLink": "",
                    "blockStartTime": "12:00:00",
                    "blockEndTime": "13:00:00",
                    "xLocation": 37.5634,
                    "yLocation": 126.9850,
                    "placeId": "",
                    "placeCategoryId": 2,
                    "timeTableId": 202
                  }
                }
              ]
            }
            
            ---

            ### 🔹 최종 지시
            - 사용자의 요청을 분석하여 `ChatBotActionResponse` JSON을 생성하라.
            - `create` 시 `target` 내부에 **모든 필드 값(좌표, 주소 포함)이 채워져 있는지** 마지막으로 확인하고 응답하라.
            - JSON 포맷만 반환하라.
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