package com.example.planmate.controller;

import com.example.planmate.dto.*;
import com.example.planmate.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @GetMapping("/preferredTheme")
    public ResponseEntity<GetPreferredThemeResponse> getPreferredTheme() {
        GetPreferredThemeResponse response = userService.getPreferredTheme();
        return ResponseEntity.ok(response);
    }
    @PostMapping("/preferredTheme")
    public ResponseEntity<SavePreferredThemeResponse> savePreferredTheme(Authentication authentication, @RequestBody SavePreferredThemeRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        SavePreferredThemeResponse response = userService.savePreferredTheme(userId, request.getPreferredThemeIds());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/profile")
    public ResponseEntity<MoveMypageResponse> moveMypage(Authentication authentication) {
        int userId = Integer.parseInt(authentication.getName());
        MoveMypageResponse response = userService.getMypageInfo(userId);
        return ResponseEntity.ok(response);
    }
    @PatchMapping("/age")
    public ResponseEntity<ChangeAgeResponse> changeAge(Authentication authentication, @RequestBody ChangeAgeRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        ChangeAgeResponse response = userService.changeAge(userId, request.getAge());
        return ResponseEntity.ok(response);
    }
    @PatchMapping("/gender")
    public ResponseEntity<ChangeGenderResponse> changeGender(Authentication authentication, @RequestBody ChangeGenderRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        ChangeGenderResponse response = userService.changeGender(userId, request.getGender());
        return ResponseEntity.ok(response);
    }
    @PatchMapping("/preferredThemes")
    public ResponseEntity<ChangePreferredThemesResponse> changeGender(Authentication authentication, @RequestBody ChangePreferredThemesRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        ChangePreferredThemesResponse response = userService.changePreferredThemes(userId, request.getPreferredThemeCategoryId(), request.getPreferredThemeIds());
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/account")
    public ResponseEntity<ResignAccountResponse> changeGender(Authentication authentication) {
        int userId = Integer.parseInt(authentication.getName());
        ResignAccountResponse response = userService.resignAccount(userId);
        return ResponseEntity.ok(response);
    }
}
