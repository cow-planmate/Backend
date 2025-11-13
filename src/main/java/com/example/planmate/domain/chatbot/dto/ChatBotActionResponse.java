package com.example.planmate.domain.chatbot.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatBotActionResponse {
    private String userMessage;  // 사용자에게 보여줄 친근한 메시지
    private boolean hasAction;   // 액션이 있는지 여부
    private List<ActionData> actions;   // 시스템 액션 데이터
    
    @Data
    @NoArgsConstructor 
    @AllArgsConstructor
    public static class ActionData {
        private String action;      // create, update, delete
        private String targetName;  // plan, timeTable, timeTablePlaceBlock
        private Object target;      // 실제 데이터 객체
    }
    
    // 액션 없는 일반 응답용
    public static ChatBotActionResponse simpleMessage(String message) {
        return new ChatBotActionResponse(message, false, null);
    }
    public void addAction(ActionData actionData) {
        this.actions.add(actionData);
    }
    
    // 액션 있는 응답용
    public static ChatBotActionResponse withAction(String message, String action, String targetName, Object target) {
        ActionData actionData = new ActionData(action, targetName, target);
        return new ChatBotActionResponse(message, true, actionData);
    }
}