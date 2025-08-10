package com.example.planmate.common.log;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class WsAccessLogInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var acc = StompHeaderAccessor.wrap(message);

        // CONNECT 시 토큰 검증 → userId 세팅
        if (StompCommand.CONNECT.equals(acc.getCommand())) {
            String auth = first(acc.getNativeHeader("Authorization")); // "Bearer xxx"
            Integer userId = validateAndGetUserId(auth);               // 직접 구현
            if (userId != null) {
                acc.setUser(new UsernamePasswordAuthenticationToken(userId, null, List.of()));
            }
        }

        // MDC 세팅 (매 메시지 단위)
        if (acc.getUser() != null) MDC.put("userId", String.valueOf(acc.getUser().getName()));
        MDC.put("traceId", UUID.randomUUID().toString().substring(0, 8));

        // 접근 로그
        if (acc.getCommand() != null) {
            int len = (message.getPayload() instanceof byte[] b) ? b.length : 0;
            log.info("WS {} dest={} session={} payloadBytes={}", acc.getCommand(),
                    acc.getDestination(), acc.getSessionId(), len);
        }
        return message;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        try {
            if (ex != null) log.warn("WS send error: {}", ex.getMessage());
        } finally {
            MDC.clear(); // 스레드 재사용 대비
        }
    }

    private String first(List<String> v){ return (v==null||v.isEmpty())?null:v.get(0); }
    private Integer validateAndGetUserId(String auth){ /* TODO */ return null; }
}
