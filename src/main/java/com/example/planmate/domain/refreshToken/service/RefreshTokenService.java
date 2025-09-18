package com.example.planmate.domain.refreshToken.service;

import org.springframework.stereotype.Service;

import com.example.planmate.common.auth.JwtTokenProvider;
import com.example.planmate.domain.refreshToken.dto.RefreshTokenResponse;
import com.example.planmate.infrastructure.redis.RefreshTokenCacheService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenCacheService refreshTokenCacheService;
    private final JwtTokenProvider jwtTokenProvider;
    public RefreshTokenResponse getToken(String refreshToken) {
        RefreshTokenResponse response = new RefreshTokenResponse();
    Integer userId = refreshTokenCacheService.findUserId(refreshToken);
        if(userId != null){
            response.setAccessToken(jwtTokenProvider.generateAccessToken(userId));
        }
        return response;
    }
}
