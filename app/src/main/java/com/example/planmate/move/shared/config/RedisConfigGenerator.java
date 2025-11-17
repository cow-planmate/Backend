package com.example.planmate.move.shared.config;

import java.util.Set;

import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.example.planmate.move.shared.framework.annotation.AutoRedisTemplate;
import com.example.planmate.move.shared.framework.dto.CacheDto;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.example.planmate.move.shared.framework.annotation.Cache;

@EnableCaching
@Configuration
public class RedisConfigGenerator implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {

    private String basePackage = "com"; // fallback 기본값

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        String mainClassName = applicationContext.getEnvironment().getProperty("sun.java.command");
        if (mainClassName != null) {
            try {
                Class<?> mainClass = Class.forName(mainClassName.split(" ")[0]);
                Package mainPackage = mainClass.getPackage();
                if (mainPackage != null) {
                    basePackage = mainPackage.getName();
                }
            } catch (Exception e) {
                basePackage = "com";
            }
        }
    }

    // 타입 기반 주입을 위한 기본 RedisTemplate<String, Object> 빈 등록
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        configureSerializers(template);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(
            @Value("${spring.data.redis.host:localhost}") String host,
            @Value("${spring.data.redis.port:6379}") int port
    ) {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public GenericJackson2JsonRedisSerializer redisValueSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Bean(name = "presenceRedis")
    public RedisTemplate<String, Object> presenceRedis(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        configureSerializers(template);
        template.afterPropertiesSet();
        return template;
    }



    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> cacheDtos = reflections.getTypesAnnotatedWith(Cache.class);

        for (Class<?> dtoClass : cacheDtos) {
            if (!CacheDto.class.isAssignableFrom(dtoClass)) {
                continue;
            }

            String beanName = resolveBeanName(dtoClass);
            if (registry.containsBeanDefinition(beanName)) {
                continue;
            }

            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(DynamicRedisTemplateFactoryBean.class);
            beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(dtoClass);
            beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
            registry.registerBeanDefinition(beanName, beanDefinition);
        }
    }

    private String resolveBeanName(Class<?> dtoClass) {
        AutoRedisTemplate annotation = dtoClass.getAnnotation(AutoRedisTemplate.class);
        if (annotation != null && !annotation.value().isBlank()) {
            return annotation.value();
        }

        String simpleName = dtoClass.getSimpleName();
        if (simpleName.endsWith("Dto")) {
            simpleName = simpleName.substring(0, simpleName.length() - 3);
        }
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1) + "Redis";
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // no-op
    }

    private static void configureSerializers(RedisTemplate<String, ?> template) {
        configureSerializers(template, null);
    }

    private static void configureSerializers(
            RedisTemplate<String, ?> template,
            GenericJackson2JsonRedisSerializer customSerializer
    ) {
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer serializer =
                customSerializer != null ? customSerializer : new GenericJackson2JsonRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(serializer);
        template.setDefaultSerializer(serializer);
    }

    public static class DynamicRedisTemplateFactoryBean<T>
            implements FactoryBean<RedisTemplate<String, T>> {

        @SuppressWarnings("unused")
        private final Class<T> valueType;

        private RedisConnectionFactory connectionFactory;
        private GenericJackson2JsonRedisSerializer serializer;

        public DynamicRedisTemplateFactoryBean(Class<T> valueType) {
            this.valueType = valueType;
        }

        @Autowired
        public void setConnectionFactory(RedisConnectionFactory connectionFactory) {
            this.connectionFactory = connectionFactory;
        }

        @Autowired
        public void setSerializer(GenericJackson2JsonRedisSerializer serializer) {
            this.serializer = serializer;
        }

        @Override
        public RedisTemplate<String, T> getObject() {
            RedisTemplate<String, T> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
            configureSerializers(template, serializer);
            template.afterPropertiesSet();
            return template;
        }

        @Override
        public Class<?> getObjectType() {
            return RedisTemplate.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }
    }
}
