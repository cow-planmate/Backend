package com.example.planmate.domain.refreshToken.controller;

import com.example.planmate.domain.refreshToken.dto.RefreshTokenRequest;
import com.example.planmate.domain.refreshToken.dto.RefreshTokenResponse;
import com.example.planmate.domain.refreshToken.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class RefreshTokenController {
    private final RefreshTokenService refreshTokenService;
    @GetMapping("/api/auth/token")
    public ResponseEntity<RefreshTokenResponse> getAccessToken(RefreshTokenRequest request) {
        RefreshTokenResponse response;
        response = refreshTokenService.getToken(request.getRefreshToken());
        if(response.getToken() != null) {
            return ResponseEntity.ok(response);
        }
        else{
            response.setMessage("Refresh Token이 유효하지 않거나 만료되었습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

    }
}
