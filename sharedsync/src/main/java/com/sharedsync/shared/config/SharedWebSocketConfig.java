package com.sharedsync.shared.config;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class SharedWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final SharedSyncWebSocketProperties props;
    private final List<HandshakeInterceptor> handshakeInterceptors;

    public SharedWebSocketConfig(
            SharedSyncWebSocketProperties props,
            ObjectProvider<List<HandshakeInterceptor>> interceptors
    ) {
        this.props = props;
        this.handshakeInterceptors =
                interceptors.getIfAvailable(Collections::emptyList);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint(props.getEndpoint())
                .setAllowedOrigins(props.getAllowedOrigins().toArray(new String[0]))
                .addInterceptors(handshakeInterceptors.toArray(new HandshakeInterceptor[0]))
                .withSockJS();
    }

}
