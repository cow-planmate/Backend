package com.sharedsync.framework.shared.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a DTO field that should be populated by converting the corresponding entity field
 * into another {@code EntityBackedCacheDto} via a static factory method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CacheDtoField {

    /**
     * Entity type that backs this DTO field.
     */
    Class<?> entityType();

    /**
     * Name of the static factory method on the DTO class that converts the entity into the DTO instance.
     * The method must accept a single argument of {@link #entityType()} and return the DTO type.
     */
    String factoryMethod() default "fromEntity";
}
