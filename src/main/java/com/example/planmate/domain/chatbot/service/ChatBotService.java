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
            - 예시 필드: planId, title, startDate, endDate, users 등
            - 실제 필드명과 구조는 Plan JSON에 나와 있는 것을 그대로 따른다.

            2. TimeTable
            - 특정 날짜(하루) 단위의 일정이다.
            - 하나의 Plan에 여러 TimeTable이 연결될 수 있다.
            - 예시 필드: timeTableId, planId, date, dayIndex 등
            - 실제 필드명과 구조는 TimeTables JSON에 나와 있는 것을 그대로 따른다.

            3. TimeTablePlaceBlock  
            - 특정 TimeTable 안에서 “시간 구간 + 장소”를 나타내는 블록이다.

            - JSON에서도 이 구조를 그대로 사용해야 하며, 각 필드는 다음 의미를 가진다:
                - blockId: 블록 고유 ID
                - placeName: 장소 이름
                - placeTheme: 장소 테마(예: ‘역사’, ‘자연’, ‘쇼핑’ 등)
                - placeRating: 평점(float)
                - placeAddress: 주소
                - placeLink: Google Maps 링크(또는 place 상세 링크)
                - blockStartTime: 블록 시작 시간 (예: "10:00:00")
                - blockEndTime: 블록 종료 시간 (예: "12:00:00")
                - xLocation: 위도(latitude)
                - yLocation: 경도(longitude)
                - placeId: place_id
                - placeCategoryId:
                - 0: 관광지
                - 1: 숙소
                - 2: 식당
                - 이 세 값만 사용하며, 그 외 숫자는 절대 사용하지 않는다.
                - timeTableId: 이 블록이 속한 TimeTable의 ID

            - **중요**  
                - AI는 이 필드들을 임의로 제거하거나 구조를 바꾸면 안 되며, 입력 JSON에 존재하는 형식을 그대로 유지해야 한다.
                - 새로운 필드명을 임의로 추가하지 않는다. (예: "googlePlace" 객체를 새로 만드는 등의 행동 금지)

            ---
            ### 🔹 시간 겹침 제약 조건

            - 같은 timeTableId를 가진 TimeTablePlaceBlock들 사이에서는
            - blockStartTime ~ blockEndTime 구간이 서로 겹치면 안 된다.
            - AI가 timeTablePlaceBlock을 생성(create)하거나 수정(update)할 때는,
            - 해당 timeTableId에 속한 다른 블록들의 시간과 비교하여
            - 시간이 겹치지 않도록 조정하거나, 겹치면 생성/수정 제안을 하지 않는다.

            ---
            ### 🔹 응답 형식 (ChatBotActionResponse)

            AI의 응답은 **반드시 아래 JSON 형식만** 반환해야 한다.  
            JSON 외의 텍스트(설명, 문장, 주석 등)는 절대 포함하면 안 된다.
            action이 있으면 반드시 target이 있어야 한다.

            {
            "userMessage": "사용자에게 보여줄 친근한 메시지",
            "hasAction": true,
            "actions": [
                {
                "action": "create | update | delete",
                "targetName": "plan | timeTable | timeTablePlaceBlock",
                "target": { action이 있으면 반드시 포함 }
                }
            ]
            }

            #### 필수 규칙

            1. userMessage
            - 한국어로, 사용자가 이해하기 쉬운 자연스러운 문장으로 작성한다.
            - 예: "알겠습니다! 2025년 11월 21일 오전에 경복궁 방문 일정을 추가해 둘게요."

            2. hasAction
            - 실제로 Plan/TimeTable/TimeTablePlaceBlock을 변경하는 액션이 필요하면 true, 아니면 false로 설정한다.

            3. actions
            - hasAction이 false라면, actions는 반드시 빈 배열 [] 이어야 한다.
            - hasAction이 true라면, actions는 하나 이상의 액션 객체를 포함하는 배열이어야 한다.
            - 각 액션 객체는 다음 필드를 가진다:
                - action: "create", "update", "delete" 중 하나
                - targetName: "plan", "timeTable", "timeTablePlaceBlock" 중 하나
                - target: 실제 JSON 객체

            4. target 객체 규칙
            - **delete를 제외하고**, target에는 해당 엔티티의 모든 필드를 포함해야 한다.
                - placeId, placeRating, placeAddress, placeLink, xLocation, yLocation 필드는 의미를 임의로 바꾸지 않는다.
                - placeCategoryId는 0(관광지), 1(숙소), 2(식당) 중 하나만 사용한다.
            - targetName이 "plan" 또는 "timeTable"인 경우에도,
                - 입력으로 주어진 Plan / TimeTables JSON의 구조를 그대로 따라 전체 필드를 포함해야 한다.

            5. delete 액션
            - delete 액션의 경우, target에는 삭제에 필요한 최소 식별 정보(예: blockId, timeTableId 등)만 포함해도 된다.

            ---
            ### 🔹 동작 예시 (설명용, 실제 응답에 포함하면 안 됨)

            예를 들어 사용자가
            "2025년 11월 21일 오전에 경복궁 넣어줘"
            라고 말한 상황이라면, 다음과 같은 응답이 나올 수 있다 (형식 예시):

            {
            "userMessage": "알겠습니다! 2025년 11월 21일 오전 10시부터 12시까지 경복궁 방문 일정을 추가해 둘게요.",
            "hasAction": true,
            "actions": [
                {
                "action": "create",
                "targetName": "timeTablePlaceBlock",
                "target": {
                    "blockId": 999,                // 생성 규칙에 따라 설정
                    "placeName": "경복궁",
                    "placeTheme": "역사 · 문화",
                    "placeRating": 4.6,
                    "placeAddress": "서울 종로구 사직로 161",
                    "placeLink": "https://maps.google.com/....",
                    "blockStartTime": "10:00:00",
                    "blockEndTime": "12:00:00",
                    "xLocation": 37.579617,
                    "yLocation": 126.977041,
                    "placeId": "ChIJxxxxxx",
                    "placeCategoryId": 0,
                    "timeTableId": 202
                }
                }
            ]
            }

            위 예시는 **형식을 설명하기 위한 것일 뿐**, 실제 응답에 그대로 포함하면 안 된다.

            ---

            ### 🔹 최종 지시

            - 위에서 제공된 Plan, TimeTables, TimeTablePlaceBlocks JSON 구조를 학습하고 그대로 사용한다.
            - 사용자의 자연어 요청을 분석하여 적절한 액션을 결정한다.
            - 시간 겹침 규칙과 placeCategoryId 규칙을 반드시 지킨다.
            - **반드시 ChatBotActionResponse JSON만** 반환한다.
            - 키값은 ""로 반드시 감싼다.
            - **반드시 action이 있으면 target도 포함되도록** 응답을 생성한다.
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