package com.example.planmate.domain.login.service;

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

    public LoginResponse login(String email, String password) {
        LoginResponse response = new LoginResponse();
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
            response.setMessage("Login successful");
            response.setLoginSuccess(true);
            return response;
        } catch (AuthenticationException e) {
            response.setMessage("Invalid username or password");
            response.setLoginSuccess(false);
            return response;
        }
    }

    public LogoutResponse logout(String refreshToken) {
    Integer userId1 = refreshTokenStore.findUserIdByRefreshToken(refreshToken);
        if(userId1 == null) {
            LogoutResponse response = new LogoutResponse(false);
            response.setMessage("Invalid refresh token");
            return response;
        }
        refreshTokenStore.deleteRefreshToken(refreshToken);
        return new LogoutResponse(true);
    }
}
