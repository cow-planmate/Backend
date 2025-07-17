package com.example.planmate.controller;

import com.example.planmate.dto.ChangePasswordRequest;
import com.example.planmate.dto.ChangePasswordResponse;
import com.example.planmate.dto.VerifyPasswordRequest;
import com.example.planmate.dto.VerifyPasswordResponse;
import com.example.planmate.service.ChangePasswordService;
import com.example.planmate.service.VerifyPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/password")
public class PasswordController {
    private final VerifyPasswordService verifyPasswordService;
    private final ChangePasswordService changePasswordService;

    @PostMapping("/verify")
    public ResponseEntity<VerifyPasswordResponse> verifyPassword(Authentication authentication, @RequestBody VerifyPasswordRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        VerifyPasswordResponse response = verifyPasswordService.verifyPassword(userId, request.getPassword());
        return ResponseEntity.ok(response);
    }
    @PatchMapping("")
    public ResponseEntity<ChangePasswordResponse> changePassword(Authentication authentication, @RequestBody ChangePasswordRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        ChangePasswordResponse response = changePasswordService.changePassword(userId, request.getPassword(), request.getConfirmPassword());
        return ResponseEntity.ok(response);
    }
}
