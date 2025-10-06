package com.example.planmate.domain.chatbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ChatBotService {

    private final RestTemplate restTemplate;
    
    @Value("${google.gemini.api.key}")
    private String apiKey;
    
    @Value("${google.gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent}")
    private String apiUrl;
    
    public ChatBotService() {
        this.restTemplate = new RestTemplate();
    }
    
    public String getChatResponse(String message) {
        try {
            // Google Gemini API 요청 형식에 맞게 데이터 구성
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", message)
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
                String extractedText = extractTextFromResponse(response.getBody());
                log.info("Successfully received response from Gemini API");
                return extractedText;
            } else {
                log.error("API call failed with status: {}", response.getStatusCode());
                return "죄송합니다. API 호출에 실패했습니다.";
            }
                    
        } catch (Exception e) {
            log.error("Error in getChatResponse: {}", e.getMessage());
            return "죄송합니다. 현재 서비스에 문제가 있습니다. 잠시 후 다시 시도해주세요.";
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