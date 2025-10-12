package com.example.planmate.domain.shared.cache;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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

public abstract class SuperAutoCacheRepository<T, ID, DTO> extends AbstractCacheRepository<T, ID, DTO> {

    @Autowired
    private ApplicationContext applicationContext;

    private final Class<DTO> dtoClass;
    private final String cacheKeyPrefix;
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
        
        // keyType이 빈 문자열이면 클래스 이름에서 자동 생성
        String annotationKeyType = cacheEntityAnnotation.keyType();
        if (annotationKeyType == null || annotationKeyType.isEmpty()) {
            // PlanDto -> "plan"
            this.cacheKeyPrefix = dtoClass.getSimpleName().replace("Dto", "").toLowerCase();
        } else {
            this.cacheKeyPrefix = annotationKeyType.toLowerCase();
        }

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
        return cacheKeyPrefix + ":" + id;
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

    /**
     * ID가 null일 경우 임시 음수 ID를 생성하여 저장
     */
    @Override
    @SuppressWarnings("unchecked")
    public DTO save(DTO dto) {
        ID id = extractId(dto);
        
        // ID가 null이면 임시 음수 ID 생성
        if (id == null) {
            Integer temporaryId = generateTemporaryId();
            dto = updateDtoWithId(dto, (ID) temporaryId);
            id = extractId(dto);
        }
        
        getRedisTemplate().opsForValue().set(getRedisKey(id), dto);
        return dto;
    }

    /**
     * Redis DECR을 사용하여 원자적으로 임시 음수 ID 생성
     * Redis의 카운터를 사용하므로 동시성 문제 없이 고유한 음수 ID 보장
     * 각 엔티티 타입별로 별도의 카운터 사용
     */
    private Integer generateTemporaryId() {
        // 엔티티 타입별로 별도의 카운터 키 사용 (예: "temporary:timetableplaceblock:counter")
        String counterKey = "temporary:" + cacheKeyPrefix + ":counter";
        
        // Redis의 DECR 명령: 키가 없으면 0에서 시작해서 -1 반환, 이후 -2, -3, ...
        Long counter = getRedisTemplate().opsForValue().decrement(counterKey);
        
        return counter.intValue();
    }

    /**
     * DTO의 ID 필드를 업데이트 (Record는 새 인스턴스 생성)
     */
    @SuppressWarnings("unchecked")
    private DTO updateDtoWithId(DTO dto, ID newId) {
        try {
            // Record 타입인 경우 withXXX 메서드 찾기
            String idFieldName = idField.getName();
            String withMethodName = "with" + Character.toUpperCase(idFieldName.charAt(0)) + idFieldName.substring(1);
            
            try {
                Method withMethod = dtoClass.getMethod(withMethodName, idField.getType());
                return (DTO) withMethod.invoke(dto, newId);
            } catch (NoSuchMethodException e) {
                // withXXX 메서드가 없으면 리플렉션으로 직접 설정 시도
                throw new RuntimeException("DTO에 " + withMethodName + " 메서드가 없습니다. Record에 withBlockId 메서드를 추가해주세요.", e);
            }
        } catch (Exception e) {
            throw new RuntimeException("DTO ID 업데이트 실패: " + dto, e);
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

    /**
     * ParentId로 캐시에서 Entity 리스트 조회
     * Redis에 이미 저장된 데이터를 조회 (DB가 아닌 캐시에서)
     */
    @Override
    public List<T> findByParentId(ID parentId) {
        List<DTO> dtos = findDtosByParentId(parentId);
        
        // Entity로 변환
        return dtos.stream()
            .map(this::convertToEntity)
            .toList();
    }

    /**
     * ParentId로 캐시에서 DTO 리스트 조회
     * Redis에 이미 저장된 DTO를 직접 반환 (Entity 변환 없음)
     */
    public List<DTO> findDtosByParentId(ID parentId) {
        if (parentIdField == null) {
            throw new UnsupportedOperationException("ParentId 필드가 없습니다.");
        }
        
        // Redis에서 패턴으로 모든 키 찾기 (예: "plan:*")
        String pattern = cacheKeyPrefix + ":*";
        Set<String> keys = getRedisTemplate().keys(pattern);
        
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 모든 DTO 가져오기
        List<DTO> allDtos = getRedisTemplate().opsForValue().multiGet(keys);
        if (allDtos == null) {
            return Collections.emptyList();
        }
        
        // parentId로 필터링
        return allDtos.stream()
            .filter(dto -> dto != null)
            .filter(dto -> {
                try {
                    Object dtoParentId = parentIdField.get(dto);
                    return parentId.equals(dtoParentId);
                } catch (IllegalAccessException e) {
                    return false;
                }
            })
            .toList();
    }

    private Object[] buildEntityConverterParameters(DTO dto) throws Exception {
        Object[] params = new Object[entityConverterRepositories.length];
        
        for (int i = 0; i < entityConverterRepositories.length; i++) {
            String repoName = entityConverterRepositories[i];
            Object repository = applicationContext.getBean(repoName);
            
            // DTO에서 적절한 ID를 추출
            Object relatedId = extractRelatedId(dto, i);
            
            // ID가 null이면 null 반환
            if (relatedId == null) {
                params[i] = null;
            } else {
                try {
                    Method getReferenceMethod = repository.getClass().getMethod("getReferenceById", Object.class);
                    Object entityRef = getReferenceMethod.invoke(repository, relatedId);
                    params[i] = entityRef;
                } catch (Exception e) {
                    params[i] = null;
                }
            }
        }
        
        return params;
    }

    private Object extractRelatedId(DTO dto, int parameterIndex) {
        String repoName = entityConverterRepositories[parameterIndex];
        
        try {
            // Repository에서 엔티티 클래스 타입 가져오기
            Object repository = applicationContext.getBean(repoName);
            Class<?> entityClass = getEntityClassFromRepository(repository);
            
            if (entityClass == null) {
                return null;
            }
            
            // 엔티티에서 @Id 어노테이션이 붙은 필드 찾기
            String idFieldName = findIdFieldNameInEntity(entityClass);
            
            if (idFieldName == null) {
                return null;
            }
            
            // DTO에서 같은 이름의 필드 찾기
            Field dtoIdField = findFieldInHierarchy(dtoClass, idFieldName);
            if (dtoIdField != null) {
                dtoIdField.setAccessible(true);
                Object idValue = dtoIdField.get(dto);
                return idValue;
            }
            
            return null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("관련 ID 추출 실패: " + repoName, e);
        }
    }
    
    /**
     * Repository에서 엔티티 클래스 타입 추출
     */
    private Class<?> getEntityClassFromRepository(Object repository) {
        try {
            // Spring Data JPA Repository는 프록시 객체이므로 인터페이스를 찾아야 함
            Class<?>[] interfaces = repository.getClass().getInterfaces();
            
            for (Class<?> iface : interfaces) {
                // Repository 인터페이스 찾기
                if (iface.getName().endsWith("Repository")) {
                    // 인터페이스의 제네릭 타입 추출
                    Type[] genericInterfaces = iface.getGenericInterfaces();
                    for (Type genericInterface : genericInterfaces) {
                        if (genericInterface instanceof ParameterizedType) {
                            ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                            Type rawType = parameterizedType.getRawType();
                            
                            // JpaRepository 인터페이스 확인
                            if (rawType.getTypeName().contains("JpaRepository")) {
                                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                                if (typeArguments.length > 0) {
                                    if (typeArguments[0] instanceof Class) {
                                        return (Class<?>) typeArguments[0]; // 첫 번째 제네릭 타입이 엔티티
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 실패 시 null 반환
        }
        return null;
    }
    
    /**
     * 엔티티 클래스에서 @Id 어노테이션이 붙은 필드 이름 찾기
     */
    private String findIdFieldNameInEntity(Class<?> entityClass) {
        // 모든 필드 순회
        for (Field field : entityClass.getDeclaredFields()) {
            // @Id 어노테이션 확인
            if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
                return field.getName();
            }
        }
        return null;
    }
    
    private Field findFieldInHierarchy(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // Record의 경우 컴포넌트 확인
            for (java.lang.reflect.RecordComponent component : clazz.getRecordComponents()) {
                if (component.getName().equals(fieldName)) {
                    try {
                        return clazz.getDeclaredField(fieldName);
                    } catch (NoSuchFieldException ex) {
                        return null;
                    }
                }
            }
            return null;
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