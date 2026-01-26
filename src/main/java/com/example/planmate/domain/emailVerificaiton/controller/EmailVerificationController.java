package com.example.planmate.domain.emailVerificaiton.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.domain.emailVerificaiton.dto.EmailVerificationRequest;
import com.example.planmate.domain.emailVerificaiton.dto.EmailVerificationResponse;
import com.example.planmate.domain.emailVerificaiton.dto.SendEmailRequest;
import com.example.planmate.domain.emailVerificaiton.dto.SendEmailResponse;
import com.example.planmate.domain.emailVerificaiton.service.EmailVerificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth", description = "이메일 인증 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/email/verification")
public class EmailVerificationController {
    private final EmailVerificationService emailVerificationService;

    @Operation(summary = "이메일 인증 코드 발송", description = "회원가입 또는 비밀번호 찾기 등 목적에 따라 이메일 인증 코드를 발송합니다.")
    @PostMapping("")
    public ResponseEntity<SendEmailResponse> sendEmail(@RequestBody SendEmailRequest request) {
        SendEmailResponse response = emailVerificationService.sendVerificationCode(request.getEmail(), request.getPurpose());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "이메일 인증 코드 확인", description = "발송된 인증 코드가 유효한지 확인합니다.")
    @PostMapping("/confirm")
    public ResponseEntity<EmailVerificationResponse> verifyEmail(@RequestBody EmailVerificationRequest request) {
        EmailVerificationResponse response = emailVerificationService.registerEmailVerify(request.getEmail(), request.getPurpose(), request.getVerificationCode());
        return ResponseEntity.ok(response);
    }
}
