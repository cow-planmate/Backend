package com.sharedsync.shared.autoConfig;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.sharedsync.shared.config.RedisConfig;
import com.sharedsync.shared.config.SharedSyncWebSocketProperties;
import com.sharedsync.shared.config.SharedWebSocketConfig;

@Configuration
@EnableConfigurationProperties(SharedSyncWebSocketProperties.class)
@Import({RedisConfig.class, SharedWebSocketConfig.class})
@ComponentScan(basePackages = {"sharedsync", "com.sharedsync"})
public class SharedSyncAutoConfig {

}
