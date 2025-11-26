package com.sharedsync.shared.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "sharedsync.websocket")
public class SharedSyncWebSocketProperties {

    /**
     * WebSocket endpoint path
     * 예: /ws-plan
     */
    private String endpoint = "/ws-sharedsync";

    /**
     * Allowed Origins
     */
    private List<String> allowedOrigins = List.of("*");
}
