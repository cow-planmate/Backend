package com.example.planmate.domain.chatbot.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatBotRequest {
    private String message;
    private UUID userId;  // optional: 사용자 식별용
    private UUID planId; // optional: 현재 작업 중인 계획 ID
}