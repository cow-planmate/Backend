package com.example.planmate.domain.password.dto;

import com.example.planmate.common.dto.IRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest implements IRequest {
    private String password;
    private String confirmPassword;
}
