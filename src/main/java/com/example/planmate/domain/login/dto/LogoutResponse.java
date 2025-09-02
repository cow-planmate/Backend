package com.example.planmate.domain.login.dto;

import com.example.planmate.common.dto.CommonResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LogoutResponse extends CommonResponse {
    private boolean logoutSuccess;
}
