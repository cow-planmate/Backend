package com.example.planmate.domain.framework.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SocketField {
    /** true면 매핑 대상에서 제외 */
    boolean ignore() default false;

    /** DTO 필드명과 다를 때 alias 지정 */
    String alias() default "";
}