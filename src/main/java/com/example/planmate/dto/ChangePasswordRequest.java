package com.example.planmate.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest implements IRequest {
    private String password;
    private String confirmPassword;
}
