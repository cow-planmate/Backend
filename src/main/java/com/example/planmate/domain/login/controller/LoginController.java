package com.example.planmate.domain.login.controller;

import com.example.planmate.domain.login.dto.LoginRequest;
import com.example.planmate.domain.login.dto.LoginResponse;
import com.example.planmate.domain.login.dto.LogoutRequest;
import com.example.planmate.domain.login.dto.LogoutResponse;
import com.example.planmate.domain.login.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
@RequiredArgsConstructor
@RestController
public class LoginController {
    private final LoginService loginService;

    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse loginResponse = loginService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(loginResponse);
    }
    @PostMapping("/api/auth/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request) {
        LogoutResponse logoutResponse = loginService.logout(request.getRefreshToken());
        return ResponseEntity.ok(logoutResponse);
    }

}
