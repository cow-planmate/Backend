package com.example.planmate.common.oauth.controller;

import java.io.IOException;

import com.example.planmate.common.oauth.enums.OAuthProvider;
import com.example.planmate.common.oauth.service.OAuthLoginService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final OAuthLoginService oAuthLoginService;

    // 1) SNS 로그인 시작
    @GetMapping("/api/oauth/{provider}")
    public void redirectToProvider(@PathVariable String provider,
                                   HttpServletResponse response) throws IOException {

        OAuthProvider oAuthProvider = OAuthProvider.fromPath(provider);

        String authorizeUrl = oAuthLoginService.buildAuthorizeUrl(oAuthProvider);

        response.sendRedirect(authorizeUrl);
    }

    // 2) SNS 로그인 콜백
    @GetMapping("/api/oauth/{provider}/callback")
    public void callback(@PathVariable String provider,
                         @RequestParam("code") String code,
                         @RequestParam(value = "state", required = false) String state,
                         HttpServletResponse response) throws IOException {

        OAuthProvider oAuthProvider = OAuthProvider.fromPath(provider);

        // Service가 최종 redirect URL을 생성하도록 변경됨.
        String redirectUrl = oAuthLoginService.handleCallback(oAuthProvider, code, state);

        response.sendRedirect(redirectUrl);
    }
}

