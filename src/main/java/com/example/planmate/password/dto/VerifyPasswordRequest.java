package com.example.planmate.password.dto;

import com.example.planmate.dto.IRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyPasswordRequest implements IRequest {
    private String password;
}
