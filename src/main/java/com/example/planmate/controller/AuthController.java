package com.example.planmate.controller;

import com.example.planmate.dto.*;
import com.example.planmate.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/password/verify")
    public ResponseEntity<VerifyPasswordResponse> verifyPassword(Authentication authentication, @RequestBody VerifyPasswordRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        VerifyPasswordResponse response = authService.verifyPassword(userId, request.getPassword());
        return ResponseEntity.ok(response);
    }
    @PatchMapping("/password")
    public ResponseEntity<ChangePasswordResponse> changePassword(Authentication authentication, @RequestBody ChangePasswordRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        ChangePasswordResponse response = authService.changePassword(userId, request.getPassword(), request.getConfirmPassword());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/password/email")
    public ResponseEntity<SendTempPasswordResponse> sendTempPassword(@RequestBody SendTempPasswordRequest request) {
        SendTempPasswordResponse response = authService.sendTempPassword(request.getEmail());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/register/email")
    public ResponseEntity<SendEmailResponse> sendEmail(@RequestBody SendEmailRequest request) {
        SendEmailResponse response = authService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/register/email/verify")
    public ResponseEntity<EmailVerificationResponse> verifyEmail(@RequestBody EmailVerificationRequest request) {
        EmailVerificationResponse response = authService.registerEmailVerify(request.getEmail(), request.getVerificationCode());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/register/nickname/verify")
    public ResponseEntity<NicknameVerificationResponse> verifyNickname(@RequestBody NicknameVerificationRequest request) {
        NicknameVerificationResponse response = authService.verifyNickname(request.getNickname());
        return ResponseEntity.ok(response);
    }
}
