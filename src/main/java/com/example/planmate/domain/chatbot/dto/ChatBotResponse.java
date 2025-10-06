package com.example.planmate.domain.chatbot.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatBotResponse {
    private String response;
    private LocalDateTime timestamp;
    private boolean success;
    private String errorMessage;
    
    // 성공 응답용 생성자
    public static ChatBotResponse success(String response) {
        return new ChatBotResponse(response, LocalDateTime.now(), true, null);
    }
    
    // 오류 응답용 생성자
    public static ChatBotResponse error(String errorMessage) {
        return new ChatBotResponse(null, LocalDateTime.now(), false, errorMessage);
    }
}