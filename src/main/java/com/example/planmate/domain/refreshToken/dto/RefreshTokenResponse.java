package com.example.planmate.domain.refreshToken.dto;

import com.example.planmate.common.dto.CommonResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenResponse extends CommonResponse {
    private String accessToken;
}
