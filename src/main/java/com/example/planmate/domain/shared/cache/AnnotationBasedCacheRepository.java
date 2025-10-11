package com.example.planmate.domain.shared.cache;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.example.planmate.domain.shared.cache.annotation.CacheEntity;
import com.example.planmate.domain.shared.cache.annotation.CacheId;
import com.example.planmate.domain.shared.cache.annotation.EntityConverter;
import com.example.planmate.domain.shared.cache.annotation.ParentId;
import com.example.planmate.domain.shared.enums.ECasheKey;

/**
 * 어노테이션 기반으로 자동화된 캐시 리포지토리
 * DTO 클래스에 어노테이션만 붙이면 모든 메서드가 자동으로 구현됩니다.
 */
public abstract class AnnotationBasedCacheRepository<T, ID, DTO> extends AbstractCacheRepository<T, ID, DTO> {

    private final Class<DTO> dtoClass;
    private final ECasheKey cacheKey;
    private final Field idField;
    private final Field parentIdField;
    private final Method entityConverterMethod;

    @SuppressWarnings("unchecked")
    public AnnotationBasedCacheRepository() {
        // 제네릭 타입에서 DTO 클래스 추출
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            Type[] typeArgs = ((ParameterizedType) superClass).getActualTypeArguments();
            this.dtoClass = (Class<DTO>) typeArgs[2]; // DTO는 세 번째 타입 파라미터
        } else {
            throw new IllegalStateException("DTO 클래스를 추출할 수 없습니다.");
        }

        // @CacheEntity 어노테이션에서 키 타입 추출
        CacheEntity cacheEntityAnnotation = dtoClass.getAnnotation(CacheEntity.class);
        if (cacheEntityAnnotation == null) {
            throw new IllegalStateException(dtoClass.getSimpleName() + "에 @CacheEntity 어노테이션이 없습니다.");
        }
        this.cacheKey = cacheEntityAnnotation.keyType();

        // @CacheId 필드 찾기
        this.idField = findFieldWithAnnotation(dtoClass, CacheId.class);
        if (this.idField == null) {
            throw new IllegalStateException(dtoClass.getSimpleName() + "에 @CacheId 어노테이션이 붙은 필드가 없습니다.");
        }
        this.idField.setAccessible(true);

        // @ParentId 필드 찾기 (선택적)
        this.parentIdField = findFieldWithAnnotation(dtoClass, ParentId.class);
        if (this.parentIdField != null) {
            this.parentIdField.setAccessible(true);
        }

        // @EntityConverter 메서드 찾기
        this.entityConverterMethod = findMethodWithAnnotation(dtoClass, EntityConverter.class);
        if (this.entityConverterMethod == null) {
            throw new IllegalStateException(dtoClass.getSimpleName() + "에 @EntityConverter 어노테이션이 붙은 메서드가 없습니다.");
        }
        this.entityConverterMethod.setAccessible(true);
    }

    @Override
    protected final String getRedisKey(ID id) {
        return cacheKey.key(id);
    }

    @Override
    protected final ID extractId(DTO dto) {
        try {
            return (ID) idField.get(dto);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("ID 필드에 접근할 수 없습니다: " + idField.getName(), e);
        }
    }

    @Override
    protected final T convertToEntity(DTO dto) {
        try {
            // 간단한 변환을 위해 Repository를 직접 주입받도록 수정
            return convertDtoToEntity(dto);
        } catch (Exception e) {
            throw new RuntimeException("Entity 변환에 실패했습니다: " + dto, e);
        }
    }

    /**
     * DTO를 Entity로 변환하는 메서드
     * 하위 클래스에서 구현해야 합니다.
     */
    protected abstract T convertDtoToEntity(DTO dto);

    /**
     * 부모 ID를 추출합니다 (findByParentId에서 사용)
     */
    protected final ID extractParentId(DTO dto) {
        if (parentIdField == null) {
            throw new UnsupportedOperationException("@ParentId 어노테이션이 붙은 필드가 없습니다.");
        }
        try {
            return (ID) parentIdField.get(dto);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("ParentId 필드에 접근할 수 없습니다: " + parentIdField.getName(), e);
        }
    }

    private Field findFieldWithAnnotation(Class<?> clazz, Class<? extends java.lang.annotation.Annotation> annotationClass) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(annotationClass)) {
                return field;
            }
        }
        return null;
    }

    private Method findMethodWithAnnotation(Class<?> clazz, Class<? extends java.lang.annotation.Annotation> annotationClass) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotationClass)) {
                return method;
            }
        }
        return null;
    }
}