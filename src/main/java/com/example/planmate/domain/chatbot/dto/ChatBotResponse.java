package com.example.planmate.domain.chatbot.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "챗봇 응답 데이터")
public class ChatBotResponse {
    @Schema(description = "챗봇 답변 내용", example = "경복궁은 서울의 대표적인 궁궐입니다.")
    private String response;

    @Schema(description = "응답 생성 시간")
    private LocalDateTime timestamp;

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "에러 발생 시 메시지", nullable = true)
    private String errorMessage;

    @Schema(description = "챗봇의 제안 액션 목록")
    private List<ChatBotActionResponse.ActionData> actions; // 시스템 액션 정보
    
    // 성공 응답용 생성자 (액션 없음)
    public static ChatBotResponse success(String response) {
        return new ChatBotResponse(response, LocalDateTime.now(), true, null, null);
    }
    
    // 성공 응답용 생성자 (액션 포함)
    public static ChatBotResponse successWithAction(String response, ChatBotActionResponse.ActionData action) {
        return successWithActions(response, action == null ? null : List.of(action));
    }

    public static ChatBotResponse successWithActions(String response, List<ChatBotActionResponse.ActionData> actions) {
        return new ChatBotResponse(response, LocalDateTime.now(), true, null,
                actions == null ? null : List.copyOf(actions));
    }
    
    // 오류 응답용 생성자
    public static ChatBotResponse error(String errorMessage) {
        return new ChatBotResponse(null, LocalDateTime.now(), false, errorMessage, null);
    }
}