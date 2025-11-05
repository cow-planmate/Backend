package com.example.planmate.common.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class LoggingMdcFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        try {
            // 요청별 traceId
            MDC.put("traceId", UUID.randomUUID().toString().substring(0,8));
            // 인증된 사용자 ID 있으면 기록
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() != null) {
                MDC.put("userId", String.valueOf(auth.getPrincipal()));
            }
            chain.doFilter(req, res);
        } finally {
            MDC.clear();
        }
    }
}