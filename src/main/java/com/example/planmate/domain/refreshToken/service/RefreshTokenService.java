package com.example.planmate.domain.refreshToken.service;

import com.example.planmate.common.auth.JwtTokenProvider;
import com.example.planmate.domain.refreshToken.dto.RefreshTokenResponse;
import com.example.planmate.domain.webSocket.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RedisService redisService;
    private final JwtTokenProvider jwtTokenProvider;
    public RefreshTokenResponse getToken(String refreshToken) {
        RefreshTokenResponse response = new RefreshTokenResponse();
        Integer userId = redisService.findUserIdByRefreshToken(refreshToken);
        if(userId != null){
            response.setAccessToken(jwtTokenProvider.generateAccessToken(userId));
        }
        return response;
    }
}
