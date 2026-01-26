package com.example.planmate.common.oauth.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.common.oauth.dto.OAuthCompleteRequest;
import com.example.planmate.common.oauth.dto.OAuthCompleteResponse;
import com.example.planmate.common.oauth.dto.TokenResponse;
import com.example.planmate.common.oauth.enums.OAuthProvider;
import com.example.planmate.common.oauth.service.OAuthCompleteService;
import com.example.planmate.common.oauth.service.OAuthExchangeService;
import com.example.planmate.common.oauth.service.OAuthLoginService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth", description = "소셜 로그인(OAuth2) 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
public class OAuthController {

    private final OAuthLoginService oAuthLoginService;
    private final OAuthCompleteService oAuthCompleteService;
    private final OAuthExchangeService oAuthExchangeService;


    // 1) SNS 로그인 시작
    @Operation(summary = "SNS 로그인 시작", description = "지정된 SNS 제공자(google, kakao 등)의 로그인 페이지로 리다이렉트합니다.")
    @GetMapping("/{provider}")
    public void redirectToProvider(@PathVariable String provider,
                                   HttpServletResponse response) throws IOException {

        OAuthProvider oAuthProvider = OAuthProvider.fromPath(provider);

        String authorizeUrl = oAuthLoginService.buildAuthorizeUrl(oAuthProvider);

        response.sendRedirect(authorizeUrl);
    }

    // 2) SNS 로그인 콜백
    @Operation(summary = "SNS 로그인 콜백", description = "SNS 로그인 완료 후 서버에서 처리하는 콜백 엔드포인트입니다.")
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

    @Operation(summary = "OAuth 가입 완료", description = "소셜 로그인 후 추가 필수 정보를 입력받아 가입을 완료합니다.")
    @PostMapping("/complete")
    public OAuthCompleteResponse complete(@RequestBody OAuthCompleteRequest request) {
        return oAuthCompleteService.completeRegistration(request);
    }

    @Operation(summary = "인가 코드 교환", description = "OAuth 인가 코드를 서버 Access Token으로 교환합니다.")
    @PostMapping("/exchange")
    public TokenResponse exchange(@RequestParam String code) {
        return oAuthExchangeService.exchange(code);
    }

}

