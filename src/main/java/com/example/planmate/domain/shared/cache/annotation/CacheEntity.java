package com.example.planmate.domain.shared.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.example.planmate.domain.shared.enums.ECasheKey;

/**
 * 캐시 엔티티임을 나타내는 어노테이션
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheEntity {
    /**
     * Redis 키 타입
     */
    ECasheKey keyType();
}