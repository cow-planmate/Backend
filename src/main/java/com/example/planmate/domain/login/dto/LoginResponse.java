package com.example.planmate.domain.login.dto;

import com.example.planmate.common.dto.CommonResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse extends CommonResponse {
    private boolean loginSuccess;
    private int userId;
    private String nickname;
    private String email;
    private String accessToken;
    private String refreshToken;
}
