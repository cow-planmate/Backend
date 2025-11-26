package com.example.planmate.common.log;

import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class WsHandshakeLogInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest req, ServerHttpResponse res,
                                   WebSocketHandler h, Map<String, Object> attrs) {
        String ip = req.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null && req instanceof ServletServerHttpRequest s) ip = s.getServletRequest().getRemoteAddr();
        return true;
    }
    @Override public void afterHandshake(ServerHttpRequest r, ServerHttpResponse s, WebSocketHandler h, Exception e) {}
}

