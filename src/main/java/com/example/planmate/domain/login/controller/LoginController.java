package com.example.planmate.domain.login.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.domain.login.dto.LoginRequest;
import com.example.planmate.domain.login.dto.LoginResponse;
import com.example.planmate.domain.login.dto.LogoutRequest;
import com.example.planmate.domain.login.dto.LogoutResponse;
import com.example.planmate.domain.login.service.LoginService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth", description = "인증(로그인/로그아웃) 관련 API")
@RequiredArgsConstructor
@RestController
public class LoginController {
    private final LoginService loginService;

    @Operation(summary = "로그인", description = "이메일과 비밀번호를 사용하여 JWT 토큰(Access/Refresh)을 발급받습니다.")
    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse loginResponse = loginService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "로그아웃", description = "리프레시 토큰을 무효화하여 로그아웃 처리를 수행합니다.")
    @PostMapping("/api/auth/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request) {
        LogoutResponse logoutResponse = loginService.logout(request.getRefreshToken());
        return ResponseEntity.ok(logoutResponse);
    }

}
