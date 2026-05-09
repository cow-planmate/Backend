package com.example.planmate.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import com.example.planmate.common.sse.SseRedisSubscriber;

/**
 * Configures the Redis Pub/Sub listener for cross-node SSE fanout.
 *
 * All backend nodes subscribe to {@link #SSE_CHANNEL}.  When any node
 * publishes an {@code SseNotificationMessage} to that channel, every
 * node's {@link SseRedisSubscriber} receives it and delivers the event
 * to whichever SSE connections are locally attached for the target user.
 *
 * A separate container bean is used so this listener does not interfere
 * with the SharedSync WebSocket listener container.
 */
@Configuration
public class SseRedisConfig {

    /** Redis Pub/Sub channel name for SSE notifications. */
    public static final String SSE_CHANNEL = "sse:notifications";

    @Bean
    public RedisMessageListenerContainer sseRedisListenerContainer(
            @org.springframework.beans.factory.annotation.Qualifier("pubSubRedisConnectionFactory") RedisConnectionFactory connectionFactory,
            SseRedisSubscriber subscriber) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(subscriber, new ChannelTopic(SSE_CHANNEL));
        return container;
    }
}
