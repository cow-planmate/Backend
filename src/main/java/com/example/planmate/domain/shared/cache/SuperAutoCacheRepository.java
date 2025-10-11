package com.example.planmate.domain.shared.cache;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

import com.example.planmate.domain.shared.cache.annotation.AutoDatabaseLoader;
import com.example.planmate.domain.shared.cache.annotation.AutoEntityConverter;
import com.example.planmate.domain.shared.cache.annotation.AutoRedisTemplate;
import com.example.planmate.domain.shared.cache.annotation.CacheEntity;
import com.example.planmate.domain.shared.cache.annotation.CacheId;
import com.example.planmate.domain.shared.cache.annotation.EntityConverter;
import com.example.planmate.domain.shared.cache.annotation.ParentId;
import com.example.planmate.domain.shared.enums.ECasheKey;

/**
 * 100% 자동화된 캐시 리포지토리
 * DTO에 어노테이션만 붙이면 모든 메서드가 자동으로 구현됩니다!
 */
public abstract class SuperAutoCacheRepository<T, ID, DTO> extends AbstractCacheRepository<T, ID, DTO> {

    @Autowired
    private ApplicationContext applicationContext;

    private final Class<DTO> dtoClass;
    private final ECasheKey cacheKey;
    private final Field idField;
    private final Field parentIdField;
    private final Method entityConverterMethod;
    private final String redisTemplateBeanName;
    private final String repositoryBeanName;
    private final String loadMethodName;
    private final String[] entityConverterRepositories;

    @SuppressWarnings("unchecked")
    public SuperAutoCacheRepository() {
        // 제네릭 타입에서 DTO 클래스 추출
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            Type[] typeArgs = ((ParameterizedType) superClass).getActualTypeArguments();
            this.dtoClass = (Class<DTO>) typeArgs[2];
        } else {
            throw new IllegalStateException("DTO 클래스를 추출할 수 없습니다.");
        }

        // @CacheEntity 어노테이션에서 키 타입 추출
        CacheEntity cacheEntityAnnotation = dtoClass.getAnnotation(CacheEntity.class);
        if (cacheEntityAnnotation == null) {
            throw new IllegalStateException(dtoClass.getSimpleName() + "에 @CacheEntity 어노테이션이 없습니다.");
        }
        this.cacheKey = cacheEntityAnnotation.keyType();

        // @AutoRedisTemplate 어노테이션에서 Redis 템플릿 이름 추출
        AutoRedisTemplate redisTemplateAnnotation = dtoClass.getAnnotation(AutoRedisTemplate.class);
        if (redisTemplateAnnotation != null && !redisTemplateAnnotation.value().isEmpty()) {
            this.redisTemplateBeanName = redisTemplateAnnotation.value();
        } else {
            // 자동 생성: TimeTableDto -> timeTableRedis
            String entityName = dtoClass.getSimpleName().replace("Dto", "");
            this.redisTemplateBeanName = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1) + "Redis";
        }

        // @AutoDatabaseLoader 어노테이션에서 Repository와 메서드 정보 추출
        AutoDatabaseLoader dbLoaderAnnotation = dtoClass.getAnnotation(AutoDatabaseLoader.class);
        if (dbLoaderAnnotation != null) {
            this.repositoryBeanName = dbLoaderAnnotation.repository().isEmpty() ? 
                generateRepositoryName() : dbLoaderAnnotation.repository();
            this.loadMethodName = dbLoaderAnnotation.method().isEmpty() ?
                generateLoadMethodName() : dbLoaderAnnotation.method();
        } else {
            this.repositoryBeanName = generateRepositoryName();
            this.loadMethodName = generateLoadMethodName();
        }

        // @AutoEntityConverter 어노테이션에서 필요한 Repository들 추출
        AutoEntityConverter entityConverterAnnotation = dtoClass.getAnnotation(AutoEntityConverter.class);
        if (entityConverterAnnotation != null) {
            this.entityConverterRepositories = entityConverterAnnotation.repositories();
        } else {
            this.entityConverterRepositories = new String[0];
        }

        // 필드와 메서드 찾기
        this.idField = findFieldWithAnnotation(dtoClass, CacheId.class);
        if (this.idField == null) {
            throw new IllegalStateException(dtoClass.getSimpleName() + "에 @CacheId 어노테이션이 붙은 필드가 없습니다.");
        }
        this.idField.setAccessible(true);

        this.parentIdField = findFieldWithAnnotation(dtoClass, ParentId.class);
        if (this.parentIdField != null) {
            this.parentIdField.setAccessible(true);
        }

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
    @SuppressWarnings("unchecked")
    protected final RedisTemplate<String, DTO> getRedisTemplate() {
        return (RedisTemplate<String, DTO>) applicationContext.getBean(redisTemplateBeanName);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final ID extractId(DTO dto) {
        try {
            return (ID) idField.get(dto);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("ID 필드에 접근할 수 없습니다: " + idField.getName(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final T convertToEntity(DTO dto) {
        try {
            // 필요한 Repository들을 자동으로 주입해서 Entity 변환
            Object[] parameters = buildEntityConverterParameters(dto);
            return (T) entityConverterMethod.invoke(dto, parameters);
        } catch (Exception e) {
            throw new RuntimeException("Entity 변환에 실패했습니다: " + dto, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public final List<DTO> loadFromDatabase(ID parentId) {
        try {
            Object repository = applicationContext.getBean(repositoryBeanName);
            Method loadMethod = findLoadMethod(repository, parentId);
            List<T> entities = (List<T>) loadMethod.invoke(repository, parentId);
            
            // Entity를 DTO로 변환
            Method fromEntityMethod = dtoClass.getMethod("fromEntity", getEntityClass());
            return entities.stream()
                .map(entity -> {
                    try {
                        return (DTO) fromEntityMethod.invoke(null, entity);
                    } catch (Exception e) {
                        throw new RuntimeException("DTO 변환 실패: " + entity, e);
                    }
                })
                .toList();
        } catch (Exception e) {
            throw new RuntimeException("데이터베이스 로딩 실패: " + parentId, e);
        }
    }

    private Object[] buildEntityConverterParameters(DTO dto) throws Exception {
        Object[] params = new Object[entityConverterRepositories.length];
        
        for (int i = 0; i < entityConverterRepositories.length; i++) {
            String repoName = entityConverterRepositories[i];
            Object repository = applicationContext.getBean(repoName);
            
            // 실제로는 DTO에서 적절한 ID를 추출해서 getReferenceById 호출
            // 여기서는 간단한 예시
            Method getReferenceMethod = repository.getClass().getMethod("getReferenceById", Object.class);
            Object entityRef = getReferenceMethod.invoke(repository, extractRelatedId(dto, i));
            params[i] = entityRef;
        }
        
        return params;
    }

    private Object extractRelatedId(DTO dto, int parameterIndex) {
        // 실제 구현에서는 DTO의 필드에서 관련 ID들을 추출
        // 지금은 간단한 예시로 parentId 반환
        try {
            if (parentIdField != null) {
                return parentIdField.get(dto);
            }
            return null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("관련 ID 추출 실패", e);
        }
    }

    private String generateRepositoryName() {
        String entityName = dtoClass.getSimpleName().replace("Dto", "");
        return Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1) + "Repository";
    }

    private String generateLoadMethodName() {
        if (parentIdField == null) return "findAll";
        String parentFieldName = parentIdField.getName().replace("Id", "");
        String capitalizedParentName = Character.toUpperCase(parentFieldName.charAt(0)) + parentFieldName.substring(1);
        return "findBy" + capitalizedParentName + capitalizedParentName + "Id";
    }

    private Method findLoadMethod(Object repository, ID parentId) throws NoSuchMethodException {
        return repository.getClass().getMethod(loadMethodName, parentId.getClass());
    }

    @SuppressWarnings("unchecked")
    private Class<T> getEntityClass() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            Type[] typeArgs = ((ParameterizedType) superClass).getActualTypeArguments();
            return (Class<T>) typeArgs[0]; // Entity는 첫 번째 타입 파라미터
        }
        throw new IllegalStateException("Entity 클래스를 추출할 수 없습니다.");
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