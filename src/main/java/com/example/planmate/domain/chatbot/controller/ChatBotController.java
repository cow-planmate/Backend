package com.example.planmate.domain.chatbot.controller;

import com.example.planmate.domain.chatbot.dto.ChatBotRequest;
import com.example.planmate.domain.chatbot.dto.ChatBotResponse;
import com.example.planmate.domain.chatbot.service.ChatBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chatbot")
public class ChatBotController {
    
    private final ChatBotService chatBotService;
    
    @PostMapping("/chat")
    public ResponseEntity<ChatBotResponse> chat(@RequestBody ChatBotRequest request) {
        try {
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ChatBotResponse.error("메시지를 입력해주세요."));
            }
            
            String response = chatBotService.getChatResponse(request.getMessage());
            return ResponseEntity.ok(ChatBotResponse.success(response));
            
        } catch (Exception e) {
            log.error("Error processing chat request: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ChatBotResponse.error("서버 내부 오류가 발생했습니다."));
        }
    }
}
