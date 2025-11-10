package com.sharedsync.framework.shared.framework.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DTO 필드에 선언하여 어떤 Repository에서 어떤 엔티티를 로드할지 지정합니다.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EntityReference {
    /** Repository bean 이름 */
    String repository();

    /** 조회할 엔티티 타입 (생략 시 toEntity 파라미터 타입 사용) */
    Class<?> entityType() default Void.class;

    /** Repository에서 호출할 메서드 이름 (기본값: findById) */
    String method() default "findById";

    /** true면 ID가 비어있어도 허용 */
    boolean optional() default true;

    /** 동일 타입이 여러 개인 경우 순서를 지정 */
    int order() default 0;
}
