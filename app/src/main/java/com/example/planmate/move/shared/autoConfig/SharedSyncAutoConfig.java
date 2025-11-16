package com.example.planmate.move.shared.autoConfig;

import com.example.planmate.move.shared.config.SharedSyncWebSocketProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SharedSyncWebSocketProperties.class)
public class SharedSyncAutoConfig {
}
