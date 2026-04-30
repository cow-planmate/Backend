package com.example.planmate.domain.refreshToken.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.planmate.common.auth.JwtTokenProvider;
import com.example.planmate.domain.refreshToken.dto.RefreshTokenResponse;
import com.example.planmate.domain.user.entity.Role;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenStore refreshTokenStore;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("getToken: 유효한 리프레시 토큰이면 새로운 엑세스 토큰을 반환한다.")
    void getToken_success() {
        // given
        String refreshToken = "refreshToken123";
        UUID userId = UUID.randomUUID();
        User user = User.builder().nickname("test").provider("local").role(Role.USER).build();
        given(refreshTokenStore.findUserIdByRefreshToken(refreshToken)).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(jwtTokenProvider.generateAccessToken(any(UUID.class), any(String.class))).willReturn("newAccessToken");

        // when
        RefreshTokenResponse response = refreshTokenService.getToken(refreshToken);

        // then
        assertEquals("newAccessToken", response.getAccessToken());
    }
}

