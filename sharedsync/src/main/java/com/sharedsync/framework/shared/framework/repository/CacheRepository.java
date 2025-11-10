package com.sharedsync.framework.shared.framework.repository;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.core.RedisTemplate;

import com.sharedsync.framework.shared.framework.annotation.AutoDatabaseLoader;
import com.sharedsync.framework.shared.framework.annotation.AutoEntityConverter;
import com.sharedsync.framework.shared.framework.annotation.AutoRedisTemplate;
import com.sharedsync.framework.shared.framework.annotation.CacheEntity;
import com.sharedsync.framework.shared.framework.annotation.CacheId;
import com.sharedsync.framework.shared.framework.annotation.EntityConverter;
import com.sharedsync.framework.shared.framework.annotation.EntityReference;
import com.sharedsync.framework.shared.framework.annotation.ParentId;
/**
 * 공통 캐시 인터페이스 - JpaRepository와 유사한 방식으로 캐시 작업을 수행
 * @param <T> 엔티티 타입
 * @param <ID> ID 타입 
 * @param <DTO> DTO 타입
 */
public abstract class CacheRepository<T, ID, DTO> {

    @Autowired
    protected ApplicationContext applicationContext;

    protected final Class<DTO> dtoClass;
    protected final String cacheKeyPrefix;
    protected final Field idField;
    protected final Field parentIdField;
    protected final Class<?> parentEntityClass;
    protected final Method entityConverterMethod;
    protected final Field entityIdField;
    protected final String redisTemplateBeanName;
    protected final String repositoryBeanName;
    protected final String loadMethodName;
    protected final String[] entityConverterRepositories;
    protected final List<Field> dtoFields;
    protected final List<EntityReferenceDefinition> entityReferenceDefinitions;

    @SuppressWarnings("unchecked")
    public CacheRepository() {
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

    List<Field> collectedFields = Arrays.stream(dtoClass.getDeclaredFields())
        .filter(field -> !Modifier.isStatic(field.getModifiers()))
        .peek(field -> field.setAccessible(true))
        .collect(Collectors.toCollection(ArrayList::new));

    this.dtoFields = Collections.unmodifiableList(collectedFields);
    this.entityReferenceDefinitions = this.dtoFields.stream()
        .filter(field -> field.isAnnotationPresent(EntityReference.class))
        .map(field -> new EntityReferenceDefinition(field, field.getAnnotation(EntityReference.class)))
        .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    // ==== CacheRepository 인터페이스 기본 CRUD 구현 ====

    
    public Optional<T> findById(ID id) {
        DTO dto = getRedisTemplate().opsForValue().get(getRedisKey(id));
        if (dto == null) {
            return Optional.empty();
        }
        return Optional.of(convertToEntity(dto));
    }

    
    public T getReferenceById(ID id) {
        return findById(id).orElseThrow(() ->
                new IllegalStateException("캐시에서 데이터를 찾을 수 없습니다: " + id));
    }


    
    public boolean existsById(ID id) {
        return Boolean.TRUE.equals(getRedisTemplate().hasKey(getRedisKey(id)));
    }

    
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
            for (Field field : dtoFields) {
                if (field.equals(idField)) {
                    continue;
                }

                Object newValue = field.get(newDto);
                if (newValue != null) {
                    field.set(existingDto, newValue);
                }
            }

            return existingDto;
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
            idField.set(dto, newId);
            return dto;
        } catch (IllegalAccessException e) {
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
    protected List<T> loadEntitiesByParentId(ID parentId) {
        try {
            Object repository = applicationContext.getBean(repositoryBeanName);
            Method loadMethod = findLoadMethod(repository, parentId);
            return (List<T>) loadMethod.invoke(repository, parentId);
        } catch (Exception e) {
            throw new RuntimeException("데이터베이스 로딩 실패: " + parentId, e);
        }
    }

    
    public final List<DTO> loadFromDatabaseByParentId(ID parentId) {
        List<T> entities = loadEntitiesByParentId(parentId);
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        List<DTO> dtos = new ArrayList<>(entities.size());
        for (T entity : entities) {
            DTO dto = convertToDto(entity);
            if (dto != null) {
                dtos.add(dto);
            }
        }
        return dtos;
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
        Class<?>[] parameterTypes = entityConverterMethod.getParameterTypes();
        Object[] params = new Object[parameterTypes.length];

        List<EntityReferenceDefinition> remainingDefinitions = new ArrayList<>(entityReferenceDefinitions);
        remainingDefinitions.sort(Comparator.comparingInt(EntityReferenceDefinition::order));

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            EntityReferenceDefinition definition = detachMatchingDefinition(remainingDefinitions, parameterType);

            if (definition != null) {
                params[i] = resolveEntityReferenceParameter(dto, definition, parameterType);
                continue;
            }

            params[i] = resolveLegacyEntityConverterParameter(dto, i);
        }

        return params;
    }

    private EntityReferenceDefinition detachMatchingDefinition(List<EntityReferenceDefinition> candidates, Class<?> parameterType) {
        for (int index = 0; index < candidates.size(); index++) {
            EntityReferenceDefinition definition = candidates.get(index);
            if (definition.matches(parameterType)) {
                candidates.remove(index);
                return definition;
            }
        }
        return null;
    }

    private Object resolveEntityReferenceParameter(DTO dto, EntityReferenceDefinition definition, Class<?> parameterType) throws Exception {
        Object relatedId = definition.readId(dto);
        if (relatedId == null) {
            if (definition.optional()) {
                return null;
            }
            throw new IllegalStateException("필수 연관 ID가 누락되었습니다: " + definition.fieldName());
        }

        Object repository = applicationContext.getBean(definition.repository());
        Method lookupMethod = resolveLookupMethod(repository, definition.method());
        if (lookupMethod == null) {
            throw new IllegalStateException("Repository '" + definition.repository() + "'에서 메서드 '"
                    + definition.method() + "'를 찾을 수 없습니다.");
        }

        Object result = lookupMethod.invoke(repository, relatedId);
        result = unwrapOptional(result);

        if (result == null) {
            if (definition.optional()) {
                return null;
            }
            throw new IllegalStateException("연관 엔티티를 찾을 수 없습니다: " + definition.fieldName() + "=" + relatedId);
        }

        if (!parameterType.isInstance(result)) {
            throw new IllegalStateException(
                    "연관 엔티티 타입 불일치. 기대 타입=" + parameterType.getName() + ", 실제 타입=" + result.getClass().getName());
        }

        return result;
    }

    private Object resolveLegacyEntityConverterParameter(DTO dto, int parameterIndex) throws Exception {
        if (parameterIndex >= entityConverterRepositories.length) {
            return null;
        }

        String repoName = entityConverterRepositories[parameterIndex];
        if (repoName == null || repoName.isBlank()) {
            return null;
        }

        Object repository = applicationContext.getBean(repoName);
        Object relatedId = extractRelatedId(dto, parameterIndex);
        if (relatedId == null) {
            return null;
        }

        Method lookupMethod = resolveLookupMethod(repository, "getReferenceById");
        if (lookupMethod == null) {
            lookupMethod = resolveLookupMethod(repository, "findById");
        }
        if (lookupMethod == null) {
            return null;
        }

        Object result = lookupMethod.invoke(repository, relatedId);
        return unwrapOptional(result);
    }

    private Method resolveLookupMethod(Object repository, String preferredMethodName) {
        Method method = findSingleArgumentMethod(repository, preferredMethodName);
        if (method != null) {
            method.setAccessible(true);
            return method;
        }

        // 기본 메서드 우선순위: preferred -> findById -> getReferenceById
        if (!"findById".equals(preferredMethodName)) {
            method = findSingleArgumentMethod(repository, "findById");
            if (method != null) {
                method.setAccessible(true);
                return method;
            }
        }

        if (!"getReferenceById".equals(preferredMethodName)) {
            method = findSingleArgumentMethod(repository, "getReferenceById");
            if (method != null) {
                method.setAccessible(true);
                return method;
            }
        }

        return null;
    }

    private Method findSingleArgumentMethod(Object repository, String methodName) {
        return Arrays.stream(repository.getClass().getMethods())
                .filter(method -> method.getName().equals(methodName) && method.getParameterCount() == 1)
                .findFirst()
                .orElse(null);
    }

    private Object unwrapOptional(Object result) {
        if (result instanceof Optional<?> optional) {
            return optional.orElse(null);
        }
        return result;
    }

    private static final class EntityReferenceDefinition {
        private final Field field;
        private final String repository;
        private final String method;
        private final boolean optional;
        private final int order;
        private final Class<?> entityType;

        private EntityReferenceDefinition(Field field, EntityReference annotation) {
            this.field = field;
            this.repository = annotation.repository();
            this.method = annotation.method();
            this.optional = annotation.optional();
            this.order = annotation.order();
            this.entityType = annotation.entityType() != Void.class ? annotation.entityType() : null;
        }

        private boolean matches(Class<?> parameterType) {
            Class<?> targetType = entityType != null ? entityType : parameterType;
            return parameterType.isAssignableFrom(targetType);
        }

        private Object readId(Object dtoInstance) {
            try {
                return field.get(dtoInstance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("연관 ID 필드 접근 실패: " + field.getName(), e);
            }
        }

        private String repository() {
            return repository;
        }

        private String method() {
            return method;
        }

        private boolean optional() {
            return optional;
        }

        private int order() {
            return order;
        }

        private String fieldName() {
            return field.getName();
        }
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
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
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
    protected Class<T> getEntityClass() {
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
    public List<DTO> findDtoListByParentId(ID parentId){
        return findDtosByParentId(parentId);
    }

    @SuppressWarnings("unchecked")
    public DTO findDtoByIdUnchecked(Object id) {
        return findDtoById((ID) id);
    }

    @SuppressWarnings("unchecked")
    public List<DTO> findDtoListByParentIdUnchecked(Object parentId) {
        return findDtoListByParentId((ID) parentId);
    }
   

    // ==== 동기화 메소드 ====

    public static void syncHierarchyToDatabaseByRootId(int rootId){
        //
    }
    public static void syncHierarchyToDatabaseByRootId(String rootId){

    }
    

    public boolean isParentIdFieldPresent() {
        return parentIdField != null;
    }

    public boolean isParentEntityOf(Class<?> potentialParentEntity) {
        return parentEntityClass != null && parentEntityClass.isAssignableFrom(potentialParentEntity);
    }

    public Class<?> getEntityType() {
        return getEntityClass();
    }

    @SuppressWarnings("unchecked")
    public Object extractIdUnchecked(Object dto) {
        return extractId((DTO) dto);
    }

    @SuppressWarnings("unchecked")
    protected ID extractEntityId(T entity) {
        if (entity == null) {
            return null;
        }
        try {
            return (ID) entityIdField.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("엔티티 ID 접근 실패", e);
        }
    }

    

    @SuppressWarnings({"unchecked", "null"})
    protected JpaRepository<T, ID> getJpaRepository() {
        return (JpaRepository<T, ID>) Objects.requireNonNull(applicationContext.getBean(repositoryBeanName));
    }


}