package com.example.planmate.domain.register.dto;

import com.example.planmate.common.dto.CommonResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RegisterResponse extends CommonResponse {
    private  boolean isRegistered;
    private UUID userId;
}
