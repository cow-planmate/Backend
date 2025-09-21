package com.example.planmate.domain.refreshToken.service;

import org.springframework.stereotype.Service;

import com.example.planmate.common.auth.JwtTokenProvider;
import com.example.planmate.domain.refreshToken.dto.RefreshTokenResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenStore refreshTokenStore;
    private final JwtTokenProvider jwtTokenProvider;
    public RefreshTokenResponse getToken(String refreshToken) {
        RefreshTokenResponse response = new RefreshTokenResponse();
    Integer userId = refreshTokenStore.findUserIdByRefreshToken(refreshToken);
        if(userId != null){
            response.setAccessToken(jwtTokenProvider.generateAccessToken(userId));
        }
        return response;
    }
}
