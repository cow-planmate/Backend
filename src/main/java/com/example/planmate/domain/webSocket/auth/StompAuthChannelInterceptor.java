package com.example.planmate.domain.webSocket.auth;

import com.example.planmate.common.auth.JwtTokenProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwt;

    public StompAuthChannelInterceptor(JwtTokenProvider jwt) { this.jwt = jwt; }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc == null) return message;

        if (StompCommand.CONNECT.equals(acc.getCommand())) {
            // 1) CONNECT 헤더에서 토큰 우선 조회 (권장)
            List<String> auths = acc.getNativeHeader("Authorization");
            String token = null;
            if (auths != null && !auths.isEmpty()) {
                String auth = auths.get(0);
                if (auth != null && auth.startsWith("Bearer ")) token = auth.substring(7);
            }
            // 2) 없으면 Handshake attributes에서 꺼내기
//            if (token == null) {
//                Authentication auth = (Authentication) acc.getSessionAttributes().get("auth");
//                if (auth != null) {
//                    acc.setUser(auth);
//                    return message;
//                }
//            } else {
//                if (!jwt.validate(token)) throw new IllegalArgumentException("Invalid JWT");
//                acc.setUser(jwt.getAuthentication(token));
//            }
        }
        return message;
    }
}

