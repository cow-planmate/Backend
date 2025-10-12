package com.example.planmate.domain.shared.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Redis 템플릿의 이름을 자동으로 지정하는 어노테이션
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoRedisTemplate {
    /**
     * Redis 템플릿 Bean 이름 (생략시 자동 생성: entityNameRedis)
     */
    String value() default "";
}