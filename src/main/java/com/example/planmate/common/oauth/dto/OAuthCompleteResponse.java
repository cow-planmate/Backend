package com.example.planmate.common.oauth.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OAuthCompleteResponse {
    private boolean success;
    private String message;
    private String accessToken;
    private String refreshToken;
    private UUID userId;
    private String nickname;
    private String email;
}
