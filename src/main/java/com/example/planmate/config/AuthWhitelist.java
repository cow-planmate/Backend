package com.example.planmate.config;

import org.springframework.util.AntPathMatcher;

import java.util.List;

public class AuthWhitelist {
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    public static final List<String> PATHS = List.of(
            "/api/auth/register/**",
            "/api/auth/login",
            "/api/auth/password/email/**",
            "/test-api-key"
    );

    public static boolean isWhitelisted(String uri) {
        return PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }
}