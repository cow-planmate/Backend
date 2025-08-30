package com.example.planmate.domain.webSocket.auth;

import com.example.planmate.common.auth.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

// JwtHandshakeInterceptor.java
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            String token = resolveToken(request);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                String userId = jwtTokenProvider.getSubject(token);
                attributes.put("userId", userId);
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest req, ServerHttpResponse res,
                               WebSocketHandler wsHandler, Exception ex) { }

    private String resolveToken(ServerHttpRequest request) {
        List<String> auths = request.getHeaders().getOrEmpty("Authorization");
        if (!auths.isEmpty() && auths.get(0).startsWith("Bearer ")) {
            return auths.get(0).substring(7);
        }
        if (request instanceof ServletServerHttpRequest sr) {
            String t = sr.getServletRequest().getParameter("token");
            if (t != null && !t.isBlank()) return t;
        }
        return null;
    }
}

