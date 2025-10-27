package com.example.planmate.domain.shared.auth;

import com.example.planmate.common.auth.JwtTokenProvider;
import com.example.planmate.domain.plan.auth.PlanAccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WsAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final PlanAccessValidator planAccessValidator;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc == null) return message;

        switch (acc.getCommand()) {
            case CONNECT -> handleConnect(acc);
            case SUBSCRIBE -> handleSubscribe(acc);
            case SEND -> handleSend(acc);
            default -> {}
        }
        return message;
    }

    private void handleConnect(StompHeaderAccessor acc) {
        String auth = firstNative(acc, "Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new AccessDeniedException("Missing Authorization");
        }
        String token = auth.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            throw new AccessDeniedException("Invalid token");
        }
        int userId = Integer.parseInt(jwtTokenProvider.getSubject(token));
        // STOMP 세션의 사용자 주체 설정
        acc.setUser(new StompPrincipal(userId));
    }

    private void handleSubscribe(StompHeaderAccessor acc) {
        handleValidate(acc, "No permission to subscribe this plan");
    }

    private void handleSend(StompHeaderAccessor acc) {
        handleValidate(acc, "No permission to send to this plan");
    }

    private void handleValidate(StompHeaderAccessor acc, String msg) {
        String dest = acc.getDestination();
        if (dest == null) return;

        Principal p = acc.getUser();
        if (p == null) throw new AccessDeniedException("Unauthenticated");

        Integer userId = ((StompPrincipal) p).userId();
        Integer planId = extractPlanId(dest);
        if (planId != null && planAccessValidator.validateUserHasAccessToPlan(userId, planId) == null) {
            throw new AccessDeniedException(msg);
        }
    }

    private String firstNative(StompHeaderAccessor acc, String key) {
        List<String> values = acc.getNativeHeader(key);
        return (values == null || values.isEmpty()) ? null : values.get(0);
    }

    private Integer extractPlanId(String dest) {
        String[] parts = dest.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("plan".equals(parts[i]) && i + 1 < parts.length) {
                try { return Integer.parseInt(parts[i + 1]); } catch (NumberFormatException ignore) {}
            }
        }
        return null;
    }

    private record StompPrincipal(Integer userId) implements Principal {
        @Override public String getName() { return "u:" + userId; }
    }
}

