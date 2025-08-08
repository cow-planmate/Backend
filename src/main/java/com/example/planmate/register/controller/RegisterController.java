package com.example.planmate.register.controller;

import com.example.planmate.register.dto.NicknameVerificationRequest;
import com.example.planmate.register.dto.NicknameVerificationResponse;
import com.example.planmate.register.dto.RegisterRequest;
import com.example.planmate.register.dto.RegisterResponse;
import com.example.planmate.register.service.RegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/register")
public class RegisterController {
    private final RegisterService registerService;

    @PostMapping("")
    public ResponseEntity<RegisterResponse> register(Authentication authentication, @RequestBody RegisterRequest request) {
        String email = authentication.getName();
        RegisterResponse response = registerService.register(email, request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/nickname/verify")
    public ResponseEntity<NicknameVerificationResponse> verifyNickname(@RequestBody NicknameVerificationRequest request) {
        NicknameVerificationResponse response = registerService.verifyNickname(request.getNickname());
        return ResponseEntity.ok(response);
    }
}
