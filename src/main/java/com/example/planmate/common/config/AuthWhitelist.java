package com.example.planmate.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

import java.util.List;
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
            "/api/plan/{planId}/complete",
            "/logs/**"
    );

    public static boolean isWhitelisted(String uri) {
        return PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }
}