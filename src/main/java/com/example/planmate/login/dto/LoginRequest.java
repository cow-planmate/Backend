package com.example.planmate.login.dto;

import com.example.planmate.dto.IRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest implements IRequest {
    private String email;
    private String password;
}
