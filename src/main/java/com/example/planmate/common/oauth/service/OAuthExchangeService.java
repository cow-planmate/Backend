package com.example.planmate.common.oauth.service;

import com.example.planmate.common.auth.JwtTokenProvider;
import com.example.planmate.common.oauth.dto.TokenResponse;
import com.example.planmate.domain.user.repository.UserRepository;
import com.example.planmate.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthExchangeService {

    private final OAuthCodeService oauthCodeService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public TokenResponse exchange(String code) {

        // 🔐 1회용 code 검증 + 소비
        int userId = oauthCodeService.consumeLoginCode(code);

        // 👤 유저 조회
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // 🎫 JWT 발급
        String accessToken = jwtTokenProvider.generateAccessToken(userId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        // 📦 DTO 반환
        return new TokenResponse(
                accessToken,
                refreshToken,
                user.getNickname(),
                user.getEmail()
        );
    }
}

