package com.example.planmate.domain.register.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.domain.register.dto.NicknameVerificationRequest;
import com.example.planmate.domain.register.dto.NicknameVerificationResponse;
import com.example.planmate.domain.register.dto.RegisterRequest;
import com.example.planmate.domain.register.dto.RegisterResponse;
import com.example.planmate.domain.register.service.RegisterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth", description = "인증(회원가입) 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/register")
public class RegisterController {
    private final RegisterService registerService;

    @Operation(summary = "회원가입", description = "추가 정보(닉네임, 성별, 나이 등)를 입력하여 회원가입을 완료합니다.")
    @PostMapping("")
    public ResponseEntity<RegisterResponse> register(Authentication authentication, @RequestBody RegisterRequest request) {
        String email = authentication.getName();
        RegisterResponse response = registerService.register(email, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "닉네임 중복 확인", description = "입력한 닉네임이 이미 사용 중인지 확인합니다.")
    @PostMapping("/nickname/verify")
    public ResponseEntity<NicknameVerificationResponse> verifyNickname(@RequestBody NicknameVerificationRequest request) {
        NicknameVerificationResponse response = registerService.verifyNickname(request.getNickname());
        return ResponseEntity.ok(response);
    }
}
