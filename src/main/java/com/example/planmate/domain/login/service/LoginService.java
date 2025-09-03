package com.example.planmate.domain.login.service;

import com.example.planmate.common.auth.JwtTokenProvider;
import com.example.planmate.domain.login.dto.LoginResponse;
import com.example.planmate.domain.login.dto.LogoutResponse;
import com.example.planmate.domain.user.CustomUserDetails;
import com.example.planmate.domain.webSocket.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

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

            response.setToken(jwtTokenProvider.generateAccessToken(userDetails.getUserId()));
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
        Integer userId1 = redisService.findUserIdByRefreshToken(refreshToken);
        if(userId1 == null) {
            LogoutResponse response = new LogoutResponse(false);
            response.setMessage("Invalid refresh token");
            return response;
        }
        redisService.deleteRefreshToken(refreshToken);
        return new LogoutResponse(true);
    }
}
