package com.example.planmate.move.shared.autoConfig;

import com.example.planmate.move.shared.presence.core.SharedPresenceFacade;
import com.example.planmate.move.shared.presence.storage.PresenceStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SharedPresenceAutoConfig {

    @Bean
    public SharedPresenceFacade sharedPresenceFacade(PresenceStorage presenceStorage) {
        return new SharedPresenceFacade(presenceStorage);
    }
}