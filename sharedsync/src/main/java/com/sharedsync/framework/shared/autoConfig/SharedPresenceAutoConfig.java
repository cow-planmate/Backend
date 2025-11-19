package com.sharedsync.framework.shared.autoConfig;

import com.sharedsync.framework.shared.presence.core.SharedPresenceFacade;
import com.sharedsync.framework.shared.presence.storage.PresenceStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SharedPresenceAutoConfig {

    @Bean
    public SharedPresenceFacade sharedPresenceFacade(PresenceStorage presenceStorage) {
        return new SharedPresenceFacade(presenceStorage);
    }
}
