package com.example.planmate.domain.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AIResponse {
    @JsonProperty("userMessage")
    private String userMessage;
    
    @JsonProperty("hasAction")
    private boolean hasAction;
    
    @JsonProperty("action")
    private ActionData action;
}