package com.example.planmate.password.dto;

import com.example.planmate.dto.IRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest implements IRequest {
    private String password;
    private String confirmPassword;
}
