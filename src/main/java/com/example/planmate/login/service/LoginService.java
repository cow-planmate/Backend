package com.example.planmate.login.service;

import com.example.planmate.auth.JwtTokenProvider;
import com.example.planmate.login.dto.LoginResponse;
import com.example.planmate.gita.CustomUserDetails;
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
            String token = jwtTokenProvider.generateToken(userDetails.getUserId());

            response.setToken(token);
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
}
