package com.example.planmate.controller;

import com.example.planmate.dto.VerifyPasswordRequest;
import com.example.planmate.dto.VerifyPasswordResponse;
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
    @PostMapping("/verify")
    public ResponseEntity<VerifyPasswordResponse> verifyPassword(Authentication authentication, @RequestBody VerifyPasswordRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        VerifyPasswordResponse response = verifyPasswordService.verifyPassword(userId, request.getPassword());
        return ResponseEntity.ok(response);
    }
}
