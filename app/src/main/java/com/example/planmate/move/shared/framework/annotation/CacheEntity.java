package com.example.planmate.move.shared.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 캐시 엔티티임을 나타내는 어노테이션
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheEntity {
    /**
     * Redis 키 타입 (빈 문자열이면 클래스 이름에서 Dto를 제거하여 자동 생성)
     * 예: PlanDto -> "plan", TimeTableDto -> "timetable"
     */
    String keyType() default "";
}