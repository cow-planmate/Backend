package com.example.planmate.domain.login.service;

import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.planmate.common.auth.JwtTokenProvider;
import com.example.planmate.domain.login.dto.LoginResponse;
import com.example.planmate.domain.login.dto.LogoutResponse;
import com.example.planmate.domain.refreshToken.service.RefreshTokenStore;
import com.example.planmate.domain.user.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final UserRepository userRepository;

    public LoginResponse login(String email, String password) {
        LoginResponse response = new LoginResponse();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElse(null);

        if (user == null) {
            response.setMessage("이메일 또는 비밀번호가 올바르지 않습니다");
            response.setLoginSuccess(false);
            return response;
        }

        if (!"local".equals(user.getProvider())) {
            response.setMessage(
                    "SNS 계정으로 가입된 이메일입니다. "
                            + user.getProvider() + " 로그인 방식을 사용하세요."
            );
            response.setLoginSuccess(false);
            return response;
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            password
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            response.setAccessToken(jwtTokenProvider.generateAccessToken(userDetails.getUserId()));
            response.setRefreshToken(jwtTokenProvider.generateRefreshToken(userDetails.getUserId()));
            response.setUserId(userDetails.getUserId());
            response.setNickname(userDetails.getNickname());
            response.setMessage("성공적으로 로그인하였습니다");
            response.setEmail(userDetails.getEmail());
            response.setLoginSuccess(true);
            return response;
        } catch (AuthenticationException e) {
            response.setMessage("이메일 또는 비밀번호가 올바르지 않습니다");
            response.setLoginSuccess(false);
            return response;
        }
    }

    public LogoutResponse logout(String refreshToken) {
    Integer userId1 = refreshTokenStore.findUserIdByRefreshToken(refreshToken);
        if(userId1 == null) {
            LogoutResponse response = new LogoutResponse(false);
            response.setMessage("유효하지 않은 토큰입니다");
            return response;
        }
        refreshTokenStore.deleteRefreshToken(refreshToken);
        return new LogoutResponse(true);
    }
}
