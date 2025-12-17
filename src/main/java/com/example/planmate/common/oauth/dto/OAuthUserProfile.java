package com.example.planmate.common.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OAuthUserProfile {

    private String providerId;   // SNS 고유 ID
    private String email;        // 이메일
    private String nickname;     // 닉네임
}
