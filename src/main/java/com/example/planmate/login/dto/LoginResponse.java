package com.example.planmate.login.dto;

import com.example.planmate.dto.CommonResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse extends CommonResponse {
    private boolean loginSuccess;
    private int userId;
    private String nickname;
    private String token;

}
