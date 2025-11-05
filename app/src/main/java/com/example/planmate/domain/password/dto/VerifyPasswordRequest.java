package com.example.planmate.domain.password.dto;

import com.example.planmate.common.dto.IRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyPasswordRequest implements IRequest {
    private String password;
}
