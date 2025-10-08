package com.example.planmate.domain.chatbot.service;

import com.example.planmate.domain.chatbot.dto.AIResponse;
import com.example.planmate.domain.chatbot.dto.ActionData;
import com.example.planmate.domain.chatbot.dto.ChatBotActionResponse;
import com.example.planmate.domain.webSocket.lazydto.PlanDto;
import com.example.planmate.domain.webSocket.lazydto.TimeTableDto;
import com.example.planmate.domain.webSocket.lazydto.TimeTablePlaceBlockDto;
import com.example.planmate.domain.webSocket.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatBotService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ChatBotPlanService chatBotPlanService;
    private final RedisService redisService;
    
    @Value("${google.gemini.api.key}")
    private String apiKey;
    
    @Value("${google.gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent}")
    private String apiUrl;
    
    public ChatBotActionResponse getChatResponse(String message, Integer planId, String planContext) {
        try {
            // 시스템 프롬프트와 함께 요청 구성
            String systemPrompt = buildSystemPrompt(planId);
            String fullMessage = systemPrompt + "\n\n";
            
            // 계획 컨텍스트 추가
            if (planContext != null) {
                fullMessage += "현재 계획 정보:\n" + planContext + "\n\n";
            }
            
            fullMessage += "사용자 메시지: " + message;
            
            if (planId != null) {
                fullMessage += "\n현재 계획 ID: " + planId;
            }
            
            // Google Gemini API 요청 형식에 맞게 데이터 구성
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", fullMessage)
                    ))
                )
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = apiUrl + "?key=" + apiKey;
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                String aiResponse = extractTextFromResponse(responseBody);
                log.info("Successfully received response from Gemini API");
                
                // AI 응답에서 함수 호출 명령어 파싱 및 실행
                if (planId != null && chatBotPlanService != null) {
                    return processAIResponseWithAction(aiResponse, planId);
                } else {
                    return ChatBotActionResponse.simpleMessage(aiResponse);
                }
            } else {
                log.error("API call failed with status: {}", response.getStatusCode());
                return ChatBotActionResponse.simpleMessage("죄송합니다. API 호출에 실패했습니다.");
            }
                    
        } catch (Exception e) {
            log.error("Error in getChatResponse: {}", e.getMessage());
            return ChatBotActionResponse.simpleMessage("죄송합니다. 현재 서비스에 문제가 있습니다. 잠시 후 다시 시도해주세요.");
        }
    }
    
    @SneakyThrows
    private String buildSystemPrompt(int planId) {
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

        // 날짜를 숫자(timestamp) 대신 문자열(yyyy-MM-dd 등)로 직렬화
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 빈 객체 예외 방지
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        String planJson = objectMapper.writeValueAsString(planDto);
        String timeTablesJson = objectMapper.writeValueAsString(timeTables);
        String timeTablePlaceBlocksJson = objectMapper.writeValueAsString(timeTablePlaceBlocks);



        return """
                당신은 여행 계획 도우미 AI입니다.
                사용자의 여행 계획을 도와주고, 필요시 계획을 수정하거나 제안할 수 있습니다.
                
                ---
                ### 🔹 역할
                - 사용자의 여행 계획 데이터를 분석하고, 필요시 수정 제안을 합니다.
                - 사용자의 요청에 따라 계획, 타임테이블, 또는 장소 블록을 생성/수정/삭제할 수 있습니다.
                - 응답은 항상 친근하고 자연스러운 메시지를 포함해야 합니다.
                
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
                ### 🔹 응답 형식 (ChatBotActionResponse)
                AI의 응답은 반드시 아래 형식을 따라야 합니다.
                **중요** 반드시 JSON으로 반환을 해야 합니다
                {
                  "userMessage": "사용자에게 보여줄 친근한 메시지",
                  "hasAction": true or false,
                  "action": {
                    "action": "create | update | delete",
                    "targetName": "plan | timeTable | timeTablePlaceBlock",
                    "target": { ... } // 실제 JSON 데이터
                  }
                }""".formatted(planJson, timeTablesJson, timeTablePlaceBlocksJson);
    }
    
    private ChatBotActionResponse processAIResponseWithAction(String aiResponse, int planId) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            
            // AI 응답에서 JSON 부분 추출
            String jsonContent = extractJsonFromResponse(aiResponse);
            if (jsonContent == null) {
                // JSON이 없으면 일반 메시지로 처리
                return ChatBotActionResponse.simpleMessage(aiResponse);
            }
            
            // JSON 파싱
            AIResponse aiResponseData = objectMapper.readValue(jsonContent, AIResponse.class);
            
            // 액션이 있는 경우 실행
            if (aiResponseData.isHasAction() && aiResponseData.getAction() != null) {
                ActionData actionData = aiResponseData.getAction();
                ChatBotActionResponse actionResult = executeAction(actionData, planId);
                
                // 액션 실행 결과와 사용자 메시지를 결합
                if (actionResult != null && actionResult.isHasAction()) {
                    // 기존 메시지와 AI 메시지를 결합
                    String combinedMessage = aiResponseData.getUserMessage();
                    if (actionResult.getUserMessage() != null && !actionResult.getUserMessage().isEmpty()) {
                        combinedMessage += "\n" + actionResult.getUserMessage();
                    }
                    return new ChatBotActionResponse(combinedMessage, true, actionResult.getAction());
                }
            }
            
            // 액션이 없거나 실행에 실패한 경우 사용자 메시지만 반환
            return ChatBotActionResponse.simpleMessage(aiResponseData.getUserMessage());
            
        } catch (Exception e) {
            log.error("Error processing AI response: {}", e.getMessage());
            // JSON 파싱에 실패하면 기존 방식으로 처리
            return processFunctionCallsWithAction(aiResponse, planId);
        }
    }
    
    private String extractJsonFromResponse(String response) {
        // JSON 형태를 찾기 위한 패턴
        int startIndex = response.indexOf("{");
        if (startIndex == -1) {
            return null;
        }
        
        int braceCount = 0;
        int endIndex = -1;
        
        for (int i = startIndex; i < response.length(); i++) {
            char c = response.charAt(i);
            if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    endIndex = i;
                    break;
                }
            }
        }
        
        if (endIndex != -1) {
            return response.substring(startIndex, endIndex + 1);
        }
        
        return null;
    }
    
    private ChatBotActionResponse executeAction(ActionData actionData, int planId) {
        try {
            String action = actionData.getAction();
            String targetName = actionData.getTargetName();
            Object target = actionData.getTarget();
            
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            
            switch (targetName) {
                case "plan":
                    if ("update".equals(action)) {
                        // target을 Map으로 변환 후 필요한 필드들 추출
                        @SuppressWarnings("unchecked")
                        Map<String, Object> planMap = (Map<String, Object>) target;
                        
                        // planName 변경
                        if (planMap.containsKey("planName")) {
                            String newName = (String) planMap.get("planName");
                            return chatBotPlanService.changePlanName(planId, newName);
                        }
                        
                        // departure 변경
                        if (planMap.containsKey("departure")) {
                            String newDeparture = (String) planMap.get("departure");
                            return chatBotPlanService.changeDeparture(planId, newDeparture);
                        }
                        
                        // 인원 수 변경
                        if (planMap.containsKey("adultCount") || planMap.containsKey("childCount")) {
                            Integer adultCount = (Integer) planMap.get("adultCount");
                            Integer childCount = (Integer) planMap.get("childCount");
                            return chatBotPlanService.changePersonCount(planId, adultCount, childCount);
                        }
                        
                        // 교통수단 변경
                        if (planMap.containsKey("transportationCategoryId")) {
                            Integer transportId = (Integer) planMap.get("transportationCategoryId");
                            return chatBotPlanService.changeTransportation(planId, transportId);
                        }
                    }
                    break;
                    
                case "timeTable":
                    // timeTable 관련 액션 처리
                    // TODO: timeTable CRUD 액션 구현
                    break;
                    
                case "timeTablePlaceBlock":
                    // timeTablePlaceBlock 관련 액션 처리
                    // TODO: timeTablePlaceBlock CRUD 액션 구현
                    break;
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error executing action: {}", e.getMessage());
            return null;
        }
    }
    
    private ChatBotActionResponse processFunctionCallsWithAction(String aiResponse, int planId) {
        // 계획 이름 변경
        Pattern planNamePattern = Pattern.compile("\\[CHANGE_PLAN_NAME:([^\\]]+)\\]");
        Matcher planNameMatcher = planNamePattern.matcher(aiResponse);
        if (planNameMatcher.find()) {
            String newName = planNameMatcher.group(1);
            return chatBotPlanService.changePlanName(planId, newName);
        }
        
        // 출발지 변경
        Pattern departurePattern = Pattern.compile("\\[CHANGE_DEPARTURE:([^\\]]+)\\]");
        Matcher departureMatcher = departurePattern.matcher(aiResponse);
        if (departureMatcher.find()) {
            String newDeparture = departureMatcher.group(1);
            return chatBotPlanService.changeDeparture(planId, newDeparture);
        }
        
        // 인원 수 변경
        Pattern personPattern = Pattern.compile("\\[CHANGE_PERSON_COUNT:([^\\]]+)\\]");
        Matcher personMatcher = personPattern.matcher(aiResponse);
        if (personMatcher.find()) {
            String[] counts = personMatcher.group(1).split(",");
            Integer adultCount = counts.length > 0 ? parseIntSafely(counts[0]) : null;
            Integer childCount = counts.length > 1 ? parseIntSafely(counts[1]) : null;
            return chatBotPlanService.changePersonCount(planId, adultCount, childCount);
        }
        
        // 교통수단 변경
        Pattern transportPattern = Pattern.compile("\\[CHANGE_TRANSPORTATION:([^\\]]+)\\]");
        Matcher transportMatcher = transportPattern.matcher(aiResponse);
        if (transportMatcher.find()) {
            Integer transportId = parseIntSafely(transportMatcher.group(1));
            if (transportId != null) {
                return chatBotPlanService.changeTransportation(planId, transportId);
            }
        }
        
        // 함수 호출이 없으면 일반 메시지 반환
        return ChatBotActionResponse.simpleMessage(aiResponse);
    }
    
    private Integer parseIntSafely(String str) {
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                if (content != null) {
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
            return "응답을 받지 못했습니다.";
        } catch (Exception e) {
            log.error("Error extracting text from response: {}", e.getMessage());
            return "응답 처리 중 오류가 발생했습니다.";
        }
    }
}