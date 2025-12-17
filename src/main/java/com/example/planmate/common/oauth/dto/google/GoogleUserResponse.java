package com.example.planmate.common.oauth.dto.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GoogleUserResponse {

    private String sub;      // Google unique ID
    private String name;     // full name
    private String email;

    @JsonProperty("email_verified")
    private Boolean emailVerified;
}
