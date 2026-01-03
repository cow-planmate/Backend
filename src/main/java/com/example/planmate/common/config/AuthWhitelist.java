package com.example.planmate.common.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
@Configuration
public class AuthWhitelist {
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    public static final List<String> PATHS = List.of(
            "/api/auth/register/nickname/verify",
            "/api/auth/login",
            "/api/auth/email/verification/**",
            "/test-api-key",
            "/api/travel",
            "/api/departure",
            "/ws-plan/**",
            "/api/auth/token",
            "/api/plan/tour",
            "/api/plan/restaurant",
            "/api/plan/lodging",
            "/api/plan/place",
            "/api/plan/nextPlace",
            "/logs/**",
            "/image/**",
            "/api/oauth/**",
            "/image/**",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-ui/index.html",
            // 챗봇 테스트용 임시 추가
            "/api/test/**",
            "/api/chatbot/**",
            "/api/beta/**"
    );




    public static boolean isWhitelisted(String uri) {
        return PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }
}