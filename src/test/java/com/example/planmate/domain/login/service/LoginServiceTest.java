package com.example.planmate.domain.login.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.example.planmate.common.auth.JwtTokenProvider;
import com.example.planmate.domain.login.dto.LoginResponse;
import com.example.planmate.domain.login.dto.LogoutResponse;
import com.example.planmate.domain.refreshToken.service.RefreshTokenStore;
import com.example.planmate.domain.user.CustomUserDetails;
import com.example.planmate.domain.user.entity.Role;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenStore refreshTokenStore;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoginService loginService;

    @Test
    @DisplayName("login: 올바른 이메일과 비밀번호로 로그인 성공")
    void login_success() {
        // given
        String email = "test@example.com";
        String password = "password";
        User user = mock(User.class);
        given(user.getProvider()).willReturn("local");
        given(user.getRole()).willReturn(Role.USER);
        given(userRepository.findByEmailIgnoreCase(email)).willReturn(Optional.of(user));

        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        given(userDetails.getUserId()).willReturn(UUID.randomUUID());
        given(userDetails.getNickname()).willReturn("testUser");
        given(userDetails.getEmail()).willReturn(email);

        given(authentication.getPrincipal()).willReturn(userDetails);
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);

        given(jwtTokenProvider.generateAccessToken(any(UUID.class), any(String.class))).willReturn("accessToken");
        given(jwtTokenProvider.generateRefreshToken(any(UUID.class))).willReturn("refreshToken");

        // when
        LoginResponse response = loginService.login(email, password);

        // then
        assertTrue(response.isLoginSuccess());
        assertEquals("성공적으로 로그인하였습니다", response.getMessage());
        assertEquals("accessToken", response.getAccessToken());
    }

    @Test
    @DisplayName("logout: 리프레시 토큰이 있으면 삭제하고 로그아웃 성공")
    void logout_success() {
        // given
        String refreshToken = "refreshToken123";

        // when
        LogoutResponse response = loginService.logout(refreshToken);

        // then
        assertTrue(response.isLogoutSuccess());
        assertEquals("성공적으로 로그아웃되었습니다", response.getMessage());
        verify(refreshTokenStore).deleteRefreshToken(refreshToken);
    }
}
