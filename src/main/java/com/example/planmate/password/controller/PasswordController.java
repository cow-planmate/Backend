package com.example.planmate.password.controller;

import com.example.planmate.password.dto.*;
import com.example.planmate.password.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/password")
public class PasswordController {
    private final PasswordService passwordService;

    @PostMapping("/verify")
    public ResponseEntity<VerifyPasswordResponse> verifyPassword(Authentication authentication, @RequestBody VerifyPasswordRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        VerifyPasswordResponse response = passwordService.verifyPassword(userId, request.getPassword());
        return ResponseEntity.ok(response);
    }
    @PatchMapping("")
    public ResponseEntity<ChangePasswordResponse> changePassword(Authentication authentication, @RequestBody ChangePasswordRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        ChangePasswordResponse response = passwordService.changePassword(userId, request.getPassword(), request.getConfirmPassword());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/email")
    public ResponseEntity<SendTempPasswordResponse> sendTempPassword(Authentication authentication) {
        String email = authentication.getName();
        SendTempPasswordResponse response = passwordService.sendTempPassword(email);
        return ResponseEntity.ok(response);
    }
}
