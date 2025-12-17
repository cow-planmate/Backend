package com.example.planmate.common.oauth.dto.naver;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverUserResponse {

    private String resultcode;
    private String message;
    private Result response;

    @Getter
    @NoArgsConstructor
    public static class Result {
        private String id;
        private String email;
        private String nickname;
    }
}
