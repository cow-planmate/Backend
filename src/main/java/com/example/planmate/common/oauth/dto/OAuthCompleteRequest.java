package com.example.planmate.common.oauth.dto;

import lombok.Getter;

@Getter
public class OAuthCompleteRequest {
    private String signupId;
    private String email;
    private Integer age;
    private Integer gender;
}
