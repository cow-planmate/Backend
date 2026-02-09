package com.example.planmate.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.lettuce.core.ReadFrom;

@Configuration
public class PlanMateRedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${SPRING_DATA_REDIS_REPLICA_HOST:}")
    private String replicaHost;

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        if (replicaHost != null && !replicaHost.isEmpty()) {
            RedisStaticMasterReplicaConfiguration config = new RedisStaticMasterReplicaConfiguration(host, port);
            config.addNode(replicaHost, port);
            if (password != null && !password.isEmpty()) {
                config.setPassword(password);
            }

            LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                    .readFrom(ReadFrom.REPLICA_PREFERRED)
                    .build();

            return new LettuceConnectionFactory(config, clientConfig);
        } else {
            LettuceConnectionFactory factory = new LettuceConnectionFactory(host, port);
            if (password != null && !password.isEmpty()) {
                factory.setPassword(password);
            }
            return factory;
        }
    }

    @Bean(name = "refreshTokenRedis")
    public RedisTemplate<String, String> refreshTokenRedis(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
