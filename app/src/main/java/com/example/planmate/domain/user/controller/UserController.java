package com.example.planmate.domain.user.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.domain.user.dto.ChangeAgeRequest;
import com.example.planmate.domain.user.dto.ChangeAgeResponse;
import com.example.planmate.domain.user.dto.ChangeGenderRequest;
import com.example.planmate.domain.user.dto.ChangeGenderResponse;
import com.example.planmate.domain.user.dto.ChangeNicknameRequest;
import com.example.planmate.domain.user.dto.ChangeNicknameResponse;
import com.example.planmate.domain.user.dto.ChangePreferredThemesRequest;
import com.example.planmate.domain.user.dto.ChangePreferredThemesResponse;
import com.example.planmate.domain.user.dto.GetPreferredThemeResponse;
import com.example.planmate.domain.user.dto.MoveMypageResponse;
import com.example.planmate.domain.user.dto.ResignAccountResponse;
import com.example.planmate.domain.user.dto.SavePreferredThemeRequest;
import com.example.planmate.domain.user.dto.SavePreferredThemeResponse;
import com.example.planmate.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

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
    @PatchMapping("/nickname")
    public ResponseEntity<ChangeNicknameResponse> changeNickname(Authentication authentication, @RequestBody ChangeNicknameRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        ChangeNicknameResponse response = userService.changeNickname(userId, request.getNickname());
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
