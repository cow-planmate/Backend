package com.example.planmate.common.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OAuthSignupCache {
    private String provider;
    private String providerId;
    private String email;
    private String nickname;
}

