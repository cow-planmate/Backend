package com.sharedsync.shared.autoConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sharedsync.shared.presence.core.SharedPresenceFacade;
import com.sharedsync.shared.presence.storage.PresenceStorage;

@Configuration
public class SharedPresenceAutoConfig {

    @Bean
    public SharedPresenceFacade sharedPresenceFacade(PresenceStorage presenceStorage) {
        return new SharedPresenceFacade(presenceStorage);
    }
}
