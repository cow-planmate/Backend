package com.example.planmate.domain.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatBotRequest {
    private String message;
    private String userId;  // optional: 사용자 식별용
    private Integer planId; // optional: 현재 작업 중인 계획 ID
}