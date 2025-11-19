package com.sharedsync.framework.shared.autoConfig;

import com.sharedsync.framework.shared.config.SharedSyncWebSocketProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SharedSyncWebSocketProperties.class)
public class SharedSyncAutoConfig {
}
