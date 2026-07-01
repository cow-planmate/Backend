package com.example.planmate.common.config;

import java.util.UUID;

import com.example.planmate.common.oauth.dto.OAuthSignupCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
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

    @Value("${spring.data.redis.sentinel.master:}")
    private String sentinelMaster;

    @Value("${spring.data.redis.sentinel.nodes:}")
    private String sentinelNodes;

    @Value("${spring.data.redis.sentinel.password:${spring.data.redis.password:}}")
    private String sentinelPassword;

    /** Sentinel 기반 HA 구성이 주어졌는지 여부. 주어지면 master/replica 서비스 대신 Sentinel로 마스터를 탐색한다. */
    private boolean sentinelEnabled() {
        return sentinelMaster != null && !sentinelMaster.isBlank()
                && sentinelNodes != null && !sentinelNodes.isBlank();
    }

    private RedisSentinelConfiguration sentinelConfiguration() {
        RedisSentinelConfiguration config = new RedisSentinelConfiguration();
        config.setMaster(sentinelMaster.trim());
        for (String node : sentinelNodes.split(",")) {
            String hostPort = node.trim();
            if (hostPort.isEmpty()) {
                continue;
            }
            int idx = hostPort.lastIndexOf(':');
            String sentinelHost = idx > 0 ? hostPort.substring(0, idx) : hostPort;
            int sentinelPort = idx > 0 ? Integer.parseInt(hostPort.substring(idx + 1)) : 26379;
            config.addSentinel(new RedisNode(sentinelHost, sentinelPort));
        }
        if (password != null && !password.isEmpty()) {
            config.setPassword(RedisPassword.of(password));
        }
        if (sentinelPassword != null && !sentinelPassword.isEmpty()) {
            config.setSentinelPassword(RedisPassword.of(sentinelPassword));
        }
        return config;
    }

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        return buildReadWriteFactory();
    }

    /**
     * SharedSync 프레임워크가 @Qualifier("sharedSyncRedisConnectionFactory")로 참조하는 커넥션 팩토리를
     * 앱이 직접 제공한다. 프레임워크의 @ConditionalOnMissingBean 기본 팩토리 대신 이 Sentinel-aware 팩토리가 쓰인다.
     */
    @Bean(name = "sharedSyncRedisConnectionFactory")
    public RedisConnectionFactory sharedSyncRedisConnectionFactory() {
        return buildReadWriteFactory();
    }

    /** 읽기 분산(replica) + 쓰기(master) 커넥션 팩토리. Sentinel 설정이 있으면 Sentinel로 마스터를 탐색한다. */
    private RedisConnectionFactory buildReadWriteFactory() {
        if (sentinelEnabled()) {
            LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                    .readFrom(ReadFrom.REPLICA_PREFERRED)
                    .build();
            return new LettuceConnectionFactory(sentinelConfiguration(), clientConfig);
        }
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

    @Bean(name = "pubSubRedisConnectionFactory")
    public RedisConnectionFactory pubSubRedisConnectionFactory() {
        return buildPubSubFactory();
    }

    /**
     * SharedSync 프레임워크가 @Qualifier("pubSubConnectionFactory")로 참조하는 Pub/Sub 커넥션 팩토리를
     * 앱이 직접 제공한다. WebSocket 동기화(RedisSyncService)가 이 팩토리로 발행하므로 Sentinel-aware여야 한다.
     */
    @Bean(name = "pubSubConnectionFactory")
    public RedisConnectionFactory pubSubConnectionFactory() {
        return buildPubSubFactory();
    }

    /** Pub/Sub 전용 커넥션 팩토리. Sentinel 설정이 있으면 Sentinel을 통해 현재 마스터에 연결(failover 자동 추종). */
    private RedisConnectionFactory buildPubSubFactory() {
        if (sentinelEnabled()) {
            // Pub/Sub은 마스터에 연결되어야 하며, Sentinel 연결은 failover 시 새 마스터를 따라간다.
            LettuceConnectionFactory factory = new LettuceConnectionFactory(sentinelConfiguration());
            factory.afterPropertiesSet();
            return factory;
        }
        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(host, port);
        if (password != null && !password.isEmpty()) {
            standaloneConfig.setPassword(RedisPassword.of(password));
        }
        LettuceConnectionFactory factory = new LettuceConnectionFactory(standaloneConfig);
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean(name = "sseStringRedisTemplate")
    public StringRedisTemplate sseStringRedisTemplate(@Qualifier("pubSubRedisConnectionFactory") RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean(name = "refreshTokenRedis")
    public RedisTemplate<String, UUID> refreshTokenRedis(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, UUID> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean(name = "emailVerificationRedis")
    @Primary
    public RedisTemplate<String, Object> emailVerificationRedis(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean(name = "oauthSignupRedis")
    public RedisTemplate<String, OAuthSignupCache> oauthSignupRedis(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, OAuthSignupCache> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }


}
