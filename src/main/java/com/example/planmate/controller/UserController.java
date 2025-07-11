package com.example.planmate.controller;

import com.example.planmate.dto.GetPreferredThemeResponse;
import com.example.planmate.service.PreferredThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final PreferredThemeService preferredThemeService;

    @GetMapping("/preferredTheme")
    public ResponseEntity<GetPreferredThemeResponse> getPreferredTheme() {
        GetPreferredThemeResponse response = preferredThemeService.getPreferredTheme();
        return ResponseEntity.ok(response);
    }



}
