package com.example.planmate.common.auth;

import com.example.planmate.common.config.AuthWhitelist;
import com.example.planmate.domain.emailVerificaiton.enums.EmailVerificationPurpose;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private static final Map<EmailVerificationPurpose, String> PURPOSE_URI_MAP = Map.of(
            EmailVerificationPurpose.SIGN_UP, "/api/auth/register",
            EmailVerificationPurpose.RESET_PASSWORD, "/api/auth/password/email"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //필터 통과
        String uri = request.getRequestURI();
        if (AuthWhitelist.isWhitelisted(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            EmailVerificationPurpose purpose = jwtTokenProvider.getPurpose(token); // null or "SIGN_UP"/"RESET_PASSWORD"
            String subject = jwtTokenProvider.getSubject(token); // userId or email

            if (purpose == null) {
                // 일반 로그인 토큰 (subject = userId)
                int userId = Integer.parseInt(subject);
                Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                // 이메일 인증 토큰 (subject = email)
                if (isPurposeValidForUri(purpose, uri)) {
                    Authentication auth = new UsernamePasswordAuthenticationToken(subject, null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token purpose");
                    return;
                }
            }
        } else {
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        }

        filterChain.doFilter(request, response);

    }

    private boolean isPurposeValidForUri(EmailVerificationPurpose purpose, String uri) {
        String expectedUri = PURPOSE_URI_MAP.get(purpose);
        return expectedUri != null && uri.contains(expectedUri);
    }

    private void writeErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"" + message + "\"}");
    }


    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}

