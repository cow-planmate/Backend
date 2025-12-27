package com.example.planmate.common.oauth.service;

import com.example.planmate.common.auth.JwtTokenProvider;
import com.example.planmate.common.oauth.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthExchangeService {

    private final OAuthCodeService oauthCodeService;
    private final JwtTokenProvider jwtTokenProvider;

    public TokenResponse exchange(String code) {

        // 🔐 1회용 code 검증 + 소비
        int userId = oauthCodeService.consumeLoginCode(code);

        // 🎫 JWT 발급
        String accessToken = jwtTokenProvider.generateAccessToken(userId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        return new TokenResponse(accessToken, refreshToken);
    }
}
