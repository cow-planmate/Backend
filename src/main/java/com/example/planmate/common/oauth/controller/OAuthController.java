package com.example.planmate.common.oauth.controller;

import java.io.IOException;

import com.example.planmate.common.oauth.dto.OAuthCompleteRequest;
import com.example.planmate.common.oauth.dto.OAuthCompleteResponse;
import com.example.planmate.common.oauth.enums.OAuthProvider;
import com.example.planmate.common.oauth.service.OAuthCompleteService;
import com.example.planmate.common.oauth.service.OAuthLoginService;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
public class OAuthController {

    private final OAuthLoginService oAuthLoginService;
    private final OAuthCompleteService oAuthCompleteService;

    // 1) SNS 로그인 시작
    @GetMapping("/{provider}")
    public void redirectToProvider(@PathVariable String provider,
                                   HttpServletResponse response) throws IOException {

        OAuthProvider oAuthProvider = OAuthProvider.fromPath(provider);

        String authorizeUrl = oAuthLoginService.buildAuthorizeUrl(oAuthProvider);

        response.sendRedirect(authorizeUrl);
    }

    // 2) SNS 로그인 콜백
    @GetMapping("/{provider}/callback")
    public void callback(@PathVariable String provider,
                         @RequestParam("code") String code,
                         @RequestParam(value = "state", required = false) String state,
                         HttpServletResponse response) throws IOException {

        OAuthProvider oAuthProvider = OAuthProvider.fromPath(provider);

        // Service가 최종 redirect URL을 생성하도록 변경됨.
        String redirectUrl = oAuthLoginService.handleCallback(oAuthProvider, code, state);

        response.sendRedirect(redirectUrl);
    }

    @PostMapping("/complete")
    public OAuthCompleteResponse complete(@RequestBody OAuthCompleteRequest request) {
        return oAuthCompleteService.completeRegistration(request);
    }
}

