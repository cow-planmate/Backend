package com.example.planmate.domain.webSocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.example.planmate.common.log.WsAccessLogInterceptor;
import com.example.planmate.domain.webSocket.auth.JwtHandshakeInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final WsAccessLogInterceptor wsAccessLogInterceptor;
    private final JwtHandshakeInterceptor handshakeInterceptor;
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-plan")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOrigins("http://localhost:3000",
                        "http://localhost:63771",
                        "http://localhost:5173",
                        "https://www.planmate.site",
                        "https://planmate.site")
                .addInterceptors(handshakeInterceptor)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration reg) {
        reg.interceptors(wsAccessLogInterceptor); // 인바운드에서 로깅
    }

}