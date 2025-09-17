package com.example.planmate.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

import java.util.List;

@Configuration
public class OptionalAuthWhitelist {
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    public static final List<String> PATHS = List.of(
            "/api/plan/*/complete"  // shareToken 분기 처리용
    );

    public static boolean isWhitelisted(String uri) {
        return PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }
}
