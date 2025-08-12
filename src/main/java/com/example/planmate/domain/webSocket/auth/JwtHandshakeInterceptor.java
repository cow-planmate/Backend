package com.example.planmate.domain.webSocket.auth;

import com.example.planmate.common.auth.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwt;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            String token = extractToken(request);
            if (token == null || token.isBlank()) {
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false;
            }
            if (!jwt.validateToken(token)) {
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false;
            }

            // 권장: Authentication을 만들어 저장 (또는 Principal)
            var auth = jwt.getSubject(token); // UsernamePasswordAuthenticationToken 등
            attributes.put("auth", auth);            // 나중에 ChannelInterceptor에서 acc.setUser(auth) 가능
            attributes.put("sub", jwt.getSubject(token)); // 필요하면 subject도 같이
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
        // 1) 쿼리스트링 우선 (SockJS 폴백 호환성)
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
        // 2) Authorization 헤더 보조
        var headers = request.getHeaders();
        String auth = headers.getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7).trim();
        }
        return null;
    }
}
