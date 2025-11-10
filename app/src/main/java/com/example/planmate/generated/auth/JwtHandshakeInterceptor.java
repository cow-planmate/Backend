package com.example.planmate.generated.auth;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.example.planmate.common.auth.AuthenticationTokenResolver;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final AuthenticationTokenResolver tokenResolver;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            String token = extractToken(request);
            if (token == null || token.isBlank()) {
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false;
            }
            if (!tokenResolver.validate(token)) {
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false;
            }

            attributes.put("userId", tokenResolver.extractPrincipalId(token));
            return true;

        } catch (Exception e) {
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest req, ServerHttpResponse res,
                               WebSocketHandler h, Exception ex) {}

    private String extractToken(ServerHttpRequest request) {
        if (request instanceof org.springframework.http.server.ServletServerHttpRequest sreq) {
            String query = sreq.getServletRequest().getQueryString();
            if (query != null) {
                for (String part : query.split("&")) {
                    String[] kv = part.split("=", 2);
                    if (kv.length == 2 && "token".equals(kv[0])) {
                        try {
                            return java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8);
                        } catch (Exception ignore) {}
                    }
                }
            }
        }

        var headers = request.getHeaders();
        String auth = headers.getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7).trim();
        }
        return null;
    }
}
