package com.example.planmate.domain.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ActionData {
    @JsonProperty("action")
    private String action; // create, update, delete
    
    @JsonProperty("targetName")
    private String targetName; // plan, timeTable, timeTablePlaceBlock
    
    @JsonProperty("target")
    private Object target; // 실제 데이터 (Map 형태로 받아서 나중에 변환)
}