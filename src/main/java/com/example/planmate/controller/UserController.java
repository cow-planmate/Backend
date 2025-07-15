package com.example.planmate.controller;

import com.example.planmate.dto.*;
import com.example.planmate.service.ChangeAgeService;
import com.example.planmate.service.ChangeGenderService;
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
    private final ChangeAgeService changeAgeService;
    private final ChangeGenderService changeGenderService;

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
    @PatchMapping("/age")
    public ResponseEntity<ChangeAgeResponse> changeAge(Authentication authentication, @RequestBody ChangeAgeRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        ChangeAgeResponse response = changeAgeService.changeAge(userId, request.getAge());
        return ResponseEntity.ok(response);
    }
    @PatchMapping("/gender")
    public ResponseEntity<ChangeGenderResponse> changeGender(Authentication authentication, @RequestBody ChangeGenderRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        ChangeGenderResponse response = changeGenderService.changeGender(userId, request.getGender());
        return ResponseEntity.ok(response);
    }
}
