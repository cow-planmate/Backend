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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "User", description = "사용자 프로필 및 계정 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @Operation(summary = "선호 테마 목록 조회", description = "시스템에서 제공하는 모든 여행 선호 테마 목록을 조회합니다.")
    @GetMapping("/preferredTheme")
    public ResponseEntity<GetPreferredThemeResponse> getPreferredTheme() {
        GetPreferredThemeResponse response = userService.getPreferredTheme();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "선호 테마 저장", description = "사용자의 여행 선호 테마를 초기 저장합니다.")
    @PostMapping("/preferredTheme")
    public ResponseEntity<SavePreferredThemeResponse> savePreferredTheme(Authentication authentication, @RequestBody SavePreferredThemeRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        SavePreferredThemeResponse response = userService.savePreferredTheme(userId, request.getPreferredThemeIds());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "마이페이지 프로필 조회", description = "사용자의 닉네임, 이메일, 작성한 플랜 등 마이페이지 정보를 조회합니다.")
    @GetMapping("/profile")
    public ResponseEntity<MoveMypageResponse> moveMypage(Authentication authentication) {
        int userId = Integer.parseInt(authentication.getName());
        MoveMypageResponse response = userService.getMypageInfo(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "닉네임 변경", description = "사용자의 닉네임을 수정합니다.")
    @PatchMapping("/nickname")
    public ResponseEntity<ChangeNicknameResponse> changeNickname(Authentication authentication, @RequestBody ChangeNicknameRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        ChangeNicknameResponse response = userService.changeNickname(userId, request.getNickname());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "나이 변경", description = "사용자의 나이 정보를 수정합니다.")
    @PatchMapping("/age")
    public ResponseEntity<ChangeAgeResponse> changeAge(Authentication authentication, @RequestBody ChangeAgeRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        ChangeAgeResponse response = userService.changeAge(userId, request.getAge());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "성별 변경", description = "사용자의 성별 정보를 수정합니다.")
    @PatchMapping("/gender")
    public ResponseEntity<ChangeGenderResponse> changeGender(Authentication authentication, @RequestBody ChangeGenderRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        ChangeGenderResponse response = userService.changeGender(userId, request.getGender());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "선호 테마 수정", description = "사용자의 여행 선호 테마를 수정합니다.")
    @PatchMapping("/preferredThemes")
    public ResponseEntity<ChangePreferredThemesResponse> changeGender(Authentication authentication, @RequestBody ChangePreferredThemesRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        ChangePreferredThemesResponse response = userService.changePreferredThemes(userId, request.getPreferredThemeCategoryId(), request.getPreferredThemeIds());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원 탈퇴", description = "사용자 계정을 영구적으로 삭제하고 모든 개인 정보를 제거합니다.")
    @DeleteMapping("/account")
    public ResponseEntity<ResignAccountResponse> changeGender(Authentication authentication) {
        int userId = Integer.parseInt(authentication.getName());
        ResignAccountResponse response = userService.resignAccount(userId);
        return ResponseEntity.ok(response);
    }
}
