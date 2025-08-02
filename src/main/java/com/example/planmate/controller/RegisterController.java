package com.example.planmate.controller;

import com.example.planmate.dto.*;
import com.example.planmate.service.EmailVerificationService;
import com.example.planmate.service.NicknameVerificationService;
import com.example.planmate.service.RegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/register")
public class RegisterController {
    private final RegisterService registerService;
    private final EmailVerificationService emailVerificationService;
    private final NicknameVerificationService nicknameVerificationService;

    @PostMapping("")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        RegisterResponse response = registerService.register(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/email")
    public ResponseEntity<SendEmailResponse> sendEmail(@RequestBody SendEmailRequest request) {
        SendEmailResponse response = emailVerificationService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/email/verify")
    public ResponseEntity<EmailVerificationResponse> verifyEmail(@RequestBody EmailVerificationRequest request) {
        EmailVerificationResponse response = emailVerificationService.registerEmailVerify(request.getEmail(), request.getVerificationCode());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/nickname/verify")
    public ResponseEntity<NicknameVerificationResponse> verifyNickname(@RequestBody NicknameVerificationRequest request) {
        NicknameVerificationResponse response = nicknameVerificationService.verifyNickname(request.getNickname());
        return ResponseEntity.ok(response);
    }
}
