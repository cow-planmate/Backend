package com.example.planmate.domain.shared.framework.repository;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.core.RedisTemplate;

import com.example.planmate.domain.shared.framework.annotation.AutoDatabaseLoader;
import com.example.planmate.domain.shared.framework.annotation.AutoEntityConverter;
import com.example.planmate.domain.shared.framework.annotation.AutoRedisTemplate;
import com.example.planmate.domain.shared.framework.annotation.CacheEntity;
import com.example.planmate.domain.shared.framework.annotation.CacheId;
import com.example.planmate.domain.shared.framework.annotation.EntityConverter;
import com.example.planmate.domain.shared.framework.annotation.ParentId;

/**
 * 완전 자동화된 캐시 리포지토리
 * DTO에 어노테이션만 추가하면 모든 CRUD 및 DB 동기화 기능이 자동으로 구현됩니다.
 * 
 * @param <T> 엔티티 타입
 * @param <ID> ID 타입  
 * @param <DTO> DTO 타입
 */
public abstract class AutoCacheRepository<T, ID, DTO> implements CacheRepository<T, ID, DTO> {

    @Autowired
    private ApplicationContext applicationContext;

    private final Class<DTO> dtoClass;
    private final String cacheKeyPrefix;
    private final Field idField;
    private final Field parentIdField;
    private final Class<?> parentEntityClass;
    private final Method entityConverterMethod;
    private final Field entityIdField;
    private final String redisTemplateBeanName;
    private final String repositoryBeanName;
    private final String loadMethodName;
    private final String[] entityConverterRepositories;

    @SuppressWarnings("unchecked")
    public AutoCacheRepository() {
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
            ParentId parentIdAnnotation = this.parentIdField.getAnnotation(ParentId.class);
            if (parentIdAnnotation != null && parentIdAnnotation.value() != Object.class) {
                this.parentEntityClass = parentIdAnnotation.value();
            } else {
                this.parentEntityClass = null;
            }
        } else {
            this.parentEntityClass = null;
        }

        this.entityConverterMethod = findMethodWithAnnotation(dtoClass, EntityConverter.class);
        if (this.entityConverterMethod == null) {
            throw new IllegalStateException(dtoClass.getSimpleName() + "에 @EntityConverter 어노테이션이 붙은 메서드가 없습니다.");
        }
        this.entityConverterMethod.setAccessible(true);

        Field detectedEntityIdField = locateEntityIdField(getEntityClass());
        if (detectedEntityIdField == null) {
            throw new IllegalStateException("@Id 필드를 찾을 수 없습니다: " + getEntityClass().getSimpleName());
        }
        detectedEntityIdField.setAccessible(true);
        this.entityIdField = detectedEntityIdField;
    }

    // ==== CacheRepository 인터페이스 기본 CRUD 구현 ====
    
    @Override
    public Optional<T> findById(ID id) {
        DTO dto = getRedisTemplate().opsForValue().get(getRedisKey(id));
        if (dto == null) {
            return Optional.empty();
        }
        return Optional.of(convertToEntity(dto));
    }
    
    @Override
    public T getReferenceById(ID id) {
        return findById(id).orElseThrow(() -> 
            new IllegalStateException("캐시에서 데이터를 찾을 수 없습니다: " + id));
    }
    
    @Override
    public void deleteById(ID id) {
        getRedisTemplate().delete(getRedisKey(id));
    }
    
    @Override
    public boolean existsById(ID id) {
        return Boolean.TRUE.equals(getRedisTemplate().hasKey(getRedisKey(id)));
    }
    
    @Override
    public List<T> findAllById(Iterable<ID> ids) {
        List<String> keys = new ArrayList<>();
        ids.forEach(id -> keys.add(getRedisKey(id)));
        
        List<DTO> dtos = getRedisTemplate().opsForValue().multiGet(keys);
        if (dtos == null) {
            return Collections.emptyList();
        }
        
        return dtos.stream()
            .filter(dto -> dto != null)
            .map(this::convertToEntity)
            .toList();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<DTO> saveAll(List<DTO> dtos) {
        for (ListIterator<DTO> iterator = dtos.listIterator(); iterator.hasNext();) {
            DTO dto = iterator.next();
            ID id = extractId(dto);

            if (id == null) {
                Integer temporaryId = generateTemporaryId();
                dto = updateDtoWithId(dto, (ID) temporaryId);
                iterator.set(dto); // 리스트 내부 DTO도 갱신
                id = extractId(dto);
            }

            getRedisTemplate().opsForValue().set(getRedisKey(id), dto);
        }
        return dtos;
    }
    
    @Override
    public void deleteAllById(Iterable<ID> ids) {
        List<String> keys = new ArrayList<>();
        ids.forEach(id -> keys.add(getRedisKey(id)));
        getRedisTemplate().delete(keys);
    }
    
    // ==== 내부 헬퍼 메서드 ====
    
    protected final String getRedisKey(ID id) {
        return cacheKeyPrefix + ":" + id;
    }

    @SuppressWarnings("unchecked")
    protected final RedisTemplate<String, DTO> getRedisTemplate() {
        return (RedisTemplate<String, DTO>) applicationContext.getBean(redisTemplateBeanName);
    }

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
     * 기존 데이터를 불러와서 null이 아닌 값만 업데이트 (ID 제외)
     */
    public DTO update(DTO dto) {
        ID id = extractId(dto);
        
        if (id == null) {
            throw new IllegalArgumentException("update는 ID가 필수입니다. save를 사용하세요.");
        }
        
        DTO existingDto = getRedisTemplate().opsForValue().get(getRedisKey(id));
        if (existingDto != null) {
            dto = mergeDto(existingDto, dto);
        }
        
        getRedisTemplate().opsForValue().set(getRedisKey(id), dto);
        return dto;
    }
    
    /**
     * Entity의 필드를 다른 Entity의 null이 아닌 값으로 업데이트
     * 리플렉션을 사용하여 범용적으로 처리
     * 
     * @param target 업데이트할 대상 Entity
     * @param source 데이터를 가져올 소스 Entity (null이 아닌 값만 복사)
     */
    public void mergeEntityFields(T target, T source) {
        if (target == null || source == null) {
            throw new IllegalArgumentException("target과 source는 null일 수 없습니다.");
        }
        
        try {
            Class<?> entityClass = target.getClass();
            
            // 모든 필드를 순회하며 업데이트
            for (Field field : entityClass.getDeclaredFields()) {
                field.setAccessible(true);
                
                // ID 필드는 건너뛰기
                if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
                    continue;
                }
                
                // @ManyToOne, @OneToMany 등 관계 필드는 건너뛰기 (선택적)
                if (field.isAnnotationPresent(jakarta.persistence.ManyToOne.class) ||
                    field.isAnnotationPresent(jakarta.persistence.OneToMany.class) ||
                    field.isAnnotationPresent(jakarta.persistence.OneToOne.class) ||
                    field.isAnnotationPresent(jakarta.persistence.ManyToMany.class)) {
                    
                    // 관계 필드도 null이 아니면 업데이트
                    Object sourceValue = field.get(source);
                    if (sourceValue != null) {
                        field.set(target, sourceValue);
                    }
                    continue;
                }
                
                // source의 값이 null이 아니면 target에 설정
                Object sourceValue = field.get(source);
                if (sourceValue != null) {
                    field.set(target, sourceValue);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Entity 필드 병합 실패", e);
        }
    }
    
    /**
     * 기존 DTO와 새 DTO를 병합
     * 새 DTO의 null이 아닌 값들로 기존 DTO를 업데이트 (ID 제외)
     */
    private DTO mergeDto(DTO existingDto, DTO newDto) {
        try {
            // Record의 모든 컴포넌트를 순회하며 새 값으로 업데이트
            java.lang.reflect.RecordComponent[] components = dtoClass.getRecordComponents();
            Object[] mergedValues = new Object[components.length];
            
            for (int i = 0; i < components.length; i++) {
                java.lang.reflect.RecordComponent component = components[i];
                Method accessor = component.getAccessor();
                
                Object newValue = accessor.invoke(newDto);
                Object existingValue = accessor.invoke(existingDto);
                
                // ID 필드는 기존 값 유지
                if (component.getName().equals(idField.getName())) {
                    mergedValues[i] = existingValue;
                }
                // 새 값이 null이 아니면 새 값 사용, null이면 기존 값 유지
                else if (newValue != null) {
                    mergedValues[i] = newValue;
                } else {
                    mergedValues[i] = existingValue;
                }
            }
            
            // Record 생성자로 새 인스턴스 생성
            Class<?>[] paramTypes = new Class<?>[components.length];
            for (int i = 0; i < components.length; i++) {
                paramTypes[i] = components[i].getType();
            }
            
            java.lang.reflect.Constructor<DTO> constructor = 
                (java.lang.reflect.Constructor<DTO>) dtoClass.getDeclaredConstructor(paramTypes);
            return constructor.newInstance(mergedValues);
            
        } catch (Exception e) {
            throw new RuntimeException("DTO 병합 실패: " + newDto, e);
        }
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

    @SuppressWarnings("unchecked")
    protected final DTO convertToDto(T entity) {
        if (entity == null) {
            return null;
        }

        try {
            Method fromEntityMethod = dtoClass.getMethod("fromEntity", getEntityClass());
            return (DTO) fromEntityMethod.invoke(null, entity);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(dtoClass.getSimpleName() + "에 fromEntity 메서드가 필요합니다.", e);
        } catch (Exception e) {
            throw new RuntimeException("Entity를 DTO로 변환하는 데 실패했습니다.", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public final List<DTO> loadFromDatabaseByParentId(ID parentId) {
        try {
            Object repository = applicationContext.getBean(repositoryBeanName);
            Method loadMethod = findLoadMethod(repository, parentId);
            List<T> entities = (List<T>) loadMethod.invoke(repository, parentId);
            return entities.stream()
                .map(this::convertToDto)
                .toList();
        } catch (Exception e) {
            throw new RuntimeException("데이터베이스 로딩 실패: " + parentId, e);
        }
    }

    @SuppressWarnings("unchecked")
    public final DTO loadFromDatabaseById(ID id){
        try {
            Object repository = applicationContext.getBean(repositoryBeanName);
            Method findByIdMethod = resolveFindByIdMethod(repository);

            Object result = findByIdMethod.invoke(repository, id);

            Object entity;
            if (result instanceof java.util.Optional<?> optional) {
                if (optional.isEmpty()) {
                    return null;
                }
                entity = optional.get();
            } else {
                entity = result;
            }

            if (entity == null) {
                return null;
            }

            return convertToDto((T) entity);

        } catch (Exception e) {
            throw new RuntimeException("ID로 데이터베이스 로딩 실패: " + id, e);
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
     * ParentId로 캐시에서 Entity 리스트 삭제
     * Redis에 저장된 해당 ParentId를 가진 모든 데이터를 삭제하고 삭제된 Entity 리스트 반환
     */
    @Override
    public List<T> deleteByParentId(ID parentId) {
        if (parentIdField == null) {
            throw new UnsupportedOperationException("ParentId 필드가 없습니다.");
        }
        
        // 먼저 삭제할 DTO들을 조회
        List<DTO> dtosToDelete = findDtosByParentId(parentId);
        
        if (dtosToDelete.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 삭제할 키들 수집
        List<String> keysToDelete = dtosToDelete.stream()
            .map(dto -> getRedisKey(extractId(dto)))
            .toList();
        
        // Redis에서 삭제
        getRedisTemplate().delete(keysToDelete);
        
        // 삭제된 Entity 리스트 반환
        return dtosToDelete.stream()
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

    private Method resolveFindByIdMethod(Object repository) throws NoSuchMethodException {
        for (Method method : repository.getClass().getMethods()) {
            if ("findById".equals(method.getName()) && method.getParameterCount() == 1) {
                return method;
            }
        }
        throw new NoSuchMethodException("findById method not found on repository " + repository.getClass());
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

    private Field locateEntityIdField(Class<?> entityClass) {
        Class<?> current = entityClass;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
                    return field;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    public DTO findDtoById(ID id){
        return getRedisTemplate().opsForValue().get(getRedisKey(id));
    }
    public List<DTO> findDtoListByParentID(ID parentId){
        return findDtosByParentId(parentId);
    }

    // ==== 동기화 메소드 ====

    public void syncToDatabase(DTO dto) {
        if (dto == null) {
            return;
        }
        // 부모가 없을 때 
        if (parentIdField == null) {
            saveToDatabase(dto);
            return;
        }
        // 부모가 있을 때
        Object parentIdValue = getParentIdValue(dto);
        if (parentIdValue instanceof Number number && number.longValue() <0) {
            System.out.println("부모키가 음수라 저장할 수 없습니다");
            return;
        }

        saveToDatabase(dto);
        return;
    }

    private void saveToDatabase(DTO dto) {
        JpaRepository<T, ID> repository = getJpaRepository();
        T entity = convertToEntity(dto);

        ID previousId = extractEntityId(entity);
        boolean hasPersistentId = previousId != null && !isTemporaryId(previousId);

        if (!hasPersistentId) {
            setEntityId(entity, null);
        }

        T savedEntity;
        if (hasPersistentId && repository.existsById(previousId)) {
            T origin = repository.findById(previousId).orElse(null);
            if (origin != null) {
                mergeEntityFields(origin, entity);
                savedEntity = repository.save(origin);
            } else {
                savedEntity = repository.save(entity);
            }
        } else {
            savedEntity = repository.save(entity);
        }
        
        DTO updatedDto = convertToDto(savedEntity);
        ID cacheId = extractId(updatedDto);
        // 아이디가 음수일때 
        if (parentIdField == null && isTemporaryId(previousId) && !isTemporaryId(cacheId)) {
            propagateParentIdChange(previousId, cacheId);
        }
        if (previousId != null) {
            getRedisTemplate().delete(getRedisKey(previousId));
        }
    }
        

    private Object getParentIdValue(DTO dto) {
        if (parentIdField == null) {
            return null;
        }
        try {
            return parentIdField.get(dto);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("ParentId 필드 접근 실패", e);
        }
    }

    private boolean isTemporaryId(Object id) {
        if (id == null) {
            return false;
        }
        if (id instanceof Number number) {
            return number.longValue() < 0L;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private void propagateParentIdChange(ID temporaryParentId, ID persistedParentId) {
        if (temporaryParentId == null || persistedParentId == null) {
            return;
        }

        Map<String, AutoCacheRepository<?, ?, ?>> repositories =
            (Map<String, AutoCacheRepository<?, ?, ?>>) (Map<?, ?>) applicationContext.getBeansOfType(AutoCacheRepository.class);
        Class<T> entityClass = getEntityClass();
        for (AutoCacheRepository<?, ?, ?> repository : repositories.values()) {
            if (repository == this) {
                continue;
            }
            if (repository.parentEntityClass == null) {
                continue;
            }
            if (!repository.parentEntityClass.isAssignableFrom(entityClass)) {
                continue;
            }
            repository.updateParentReferenceInternal(temporaryParentId, persistedParentId);
        }
    }

    private void updateParentReferenceInternal(Object oldParentId, Object newParentId) {
        if (parentEntityClass == null) {
            return;
        }
        if (parentIdField == null) {
            return;
        }
        if (oldParentId == null || newParentId == null) {
            return;
        }
        if (!parentIdField.getType().isInstance(oldParentId) || !parentIdField.getType().isInstance(newParentId)) {
            return;
        }

        String pattern = cacheKeyPrefix + ":*";
        Set<String> keys = getRedisTemplate().keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return;
        }

        List<DTO> dtos = getRedisTemplate().opsForValue().multiGet(keys);
        if (dtos == null || dtos.isEmpty()) {
            return;
        }

        for (DTO dto : dtos) {
            if (dto == null) {
                continue;
            }
            try {
                Object parentValue = parentIdField.get(dto);
                if (Objects.equals(parentValue, oldParentId)) {
                    DTO updated = updateDtoParentId(dto, newParentId);
                    ID dtoId = extractId(updated);
                    getRedisTemplate().opsForValue().set(getRedisKey(dtoId), updated);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("ParentId 필드 접근 실패", e);
            }
        }
    }

    private DTO updateDtoParentId(DTO dto, Object newParentId) {
        if (parentIdField == null) {
            return dto;
        }

        try {
            java.lang.reflect.RecordComponent[] components = dtoClass.getRecordComponents();
            Object[] values = new Object[components.length];
            Class<?>[] paramTypes = new Class<?>[components.length];

            for (int i = 0; i < components.length; i++) {
                java.lang.reflect.RecordComponent component = components[i];
                Method accessor = component.getAccessor();
                paramTypes[i] = component.getType();

                Object currentValue = accessor.invoke(dto);
                if (component.getName().equals(parentIdField.getName())) {
                    values[i] = newParentId;
                } else {
                    values[i] = currentValue;
                }
            }

            java.lang.reflect.Constructor<DTO> constructor =
                (java.lang.reflect.Constructor<DTO>) dtoClass.getDeclaredConstructor(paramTypes);
            return constructor.newInstance(values);
        } catch (Exception e) {
            throw new RuntimeException("DTO 부모 ID 업데이트 실패", e);
        }
    }

    @SuppressWarnings("unchecked")
    private ID extractEntityId(T entity) {
        if (entity == null) {
            return null;
        }
        try {
            return (ID) entityIdField.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("엔티티 ID 접근 실패", e);
        }
    }

    private void setEntityId(T entity, Object value) {
        if (entity == null) {
            return;
        }
        try {
            entityIdField.set(entity, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("엔티티 ID 설정 실패", e);
        }
    }

    @SuppressWarnings("unchecked")
    private JpaRepository<T, ID> getJpaRepository() {
        return (JpaRepository<T, ID>) applicationContext.getBean(repositoryBeanName);
    }
}