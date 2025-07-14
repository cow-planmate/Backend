package com.example.planmate.controller;

import com.example.planmate.dto.GetPreferredThemeResponse;
import com.example.planmate.dto.MoveMypageResponse;
import com.example.planmate.dto.SavePreferredThemeRequest;
import com.example.planmate.dto.SavePreferredThemeResponse;
import com.example.planmate.service.MoveMypageService;
import com.example.planmate.service.PreferredThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final PreferredThemeService preferredThemeService;
    private final MoveMypageService moveMypageService;

    @GetMapping("/preferredTheme")
    public ResponseEntity<GetPreferredThemeResponse> getPreferredTheme() {
        GetPreferredThemeResponse response = preferredThemeService.getPreferredTheme();
        return ResponseEntity.ok(response);
    }
    @PostMapping("/preferredTheme")
    public ResponseEntity<SavePreferredThemeResponse> savePreferredTheme(Authentication authentication, @RequestBody SavePreferredThemeRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        SavePreferredThemeResponse response = preferredThemeService.savePreferredTheme(userId, request.getPreferredThemeIds());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/profile")
    public ResponseEntity<MoveMypageResponse> moveMypage(Authentication authentication) {
        int userId = Integer.parseInt(authentication.getName());
        MoveMypageResponse response = moveMypageService.getMypageInfo(userId);
        return ResponseEntity.ok(response);
    }
}
