package com.example.planmate.domain.login.dto;

import com.example.planmate.common.dto.IRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutRequest implements IRequest {
    private String refreshToken;
}
