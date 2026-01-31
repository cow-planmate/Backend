package com.example.planmate.domain.password.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.domain.password.dto.ChangePasswordRequest;
import com.example.planmate.domain.password.dto.ChangePasswordResponse;
import com.example.planmate.domain.password.dto.SendTempPasswordResponse;
import com.example.planmate.domain.password.dto.VerifyPasswordRequest;
import com.example.planmate.domain.password.dto.VerifyPasswordResponse;
import com.example.planmate.domain.password.service.PasswordService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth", description = "비밀번호 관리 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/password")
public class PasswordController {
    private final PasswordService passwordService;

    @Operation(summary = "비밀번호 확인", description = "중요 작업 수행 전 현재 비밀번호가 올바른지 확인합니다.")
    @PostMapping("/verify")
    public ResponseEntity<VerifyPasswordResponse> verifyPassword(Authentication authentication, @RequestBody VerifyPasswordRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        VerifyPasswordResponse response = passwordService.verifyPassword(userId, request.getPassword());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "비밀번호 변경", description = "로그인된 사용자의 비밀번호를 새로운 비밀번호로 변경합니다.")
    @PatchMapping("")
    public ResponseEntity<ChangePasswordResponse> changePassword(Authentication authentication, @RequestBody ChangePasswordRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        ChangePasswordResponse response = passwordService.changePassword(userId, request.getPassword(), request.getConfirmPassword());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "임시 비밀번호 발급", description = "비밀번호를 분실한 경우 이메일로 임시 비밀번호를 발급합니다.")
    @PostMapping("/email")
    public ResponseEntity<SendTempPasswordResponse> sendTempPassword(Authentication authentication) {
        String email = authentication.getName();
        SendTempPasswordResponse response = passwordService.sendTempPassword(email);
        return ResponseEntity.ok(response);
    }
}
