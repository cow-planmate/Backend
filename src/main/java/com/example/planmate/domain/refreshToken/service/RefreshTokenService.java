package com.example.planmate.domain.refreshToken.service;

import org.springframework.stereotype.Service;

import com.example.planmate.common.auth.JwtTokenProvider;
import com.example.planmate.domain.refreshToken.dto.RefreshTokenResponse;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenStore refreshTokenStore;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public RefreshTokenResponse getToken(String refreshToken) {
        RefreshTokenResponse response = new RefreshTokenResponse();
        UUID userId = refreshTokenStore.findUserIdByRefreshToken(refreshToken);
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            String role = (user != null) ? user.getRole().name() : "USER";
            response.setAccessToken(jwtTokenProvider.generateAccessToken(userId, role));
        }
        return response;
    }
}

