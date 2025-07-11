package com.example.planmate.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse extends CommonResponse {
    private boolean loginSuccess;
    private int userId;
    private String token;

}
