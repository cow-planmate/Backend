package com.sharedsync.framework.shared.framework.repository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sharedsync.framework.shared.framework.dto.CacheDto;

/**
 * 완전 자동화된 캐시 리포지토리
 * DTO에 어노테이션만 추가하면 모든 CRUD 및 DB 동기화 기능이 자동으로 구현됩니다.
 *
 * @param <T> 엔티티 타입
 * @param <ID> ID 타입  
 * @param <DTO> DTO 타입
 */
public abstract class AutoCacheRepository<T, ID, DTO extends CacheDto<ID>> extends CacheRepository<T, ID, DTO> {


    @SuppressWarnings("unchecked")
    public AutoCacheRepository() {
        super();
    }

    public void deleteById(ID id) {
        deleteCacheCascade(id);
    }

    public void deleteAllById(Iterable<ID> ids) {
        if (ids == null) {
            return;
        }
        ids.forEach(this::deleteCacheCascade);
    }

    private void deleteCacheCascade(ID id) {
        if (id == null) {
            return;
        }

        propagateParentDeletion(id);
        getRedisTemplate().delete(getRedisKey(id));
    }

    @SuppressWarnings("unchecked")
    private void propagateParentDeletion(Object parentIdObject) {
        if (parentIdObject == null) {
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
            repository.removeEntriesByParentInternal(parentIdObject);
        }
    }

    @SuppressWarnings("unchecked")
    private void removeEntriesByParentInternal(Object parentIdObject) {
        if (parentIdField == null || parentIdObject == null) {
            return;
        }
        if (!parentIdField.getType().isInstance(parentIdObject)) {
            return;
        }

        ID parentId = (ID) parentIdObject;
        List<DTO> dtos = findDtosByParentId(parentId);
        if (dtos.isEmpty()) {
            return;
        }

        for (DTO dto : dtos) {
            ID childId = extractId(dto);
            deleteCacheCascade(childId);
        }
    }

    public List<T> deleteByParentId(ID parentId) {
        if (parentIdField == null) {
            throw new UnsupportedOperationException("ParentId 필드가 없습니다.");
        }

        // 먼저 삭제할 DTO들을 조회
        List<DTO> dtosToDelete = findDtosByParentId(parentId);

        if (dtosToDelete.isEmpty()) {
            return Collections.emptyList();
        }

        // 하위 캐시 포함 삭제
        dtosToDelete.stream()
                .map(this::extractId)
                .forEach(this::deleteCacheCascade);

        // 삭제된 Entity 리스트 반환
        return dtosToDelete.stream()
                .map(this::convertToEntity)
                .toList();
    }

    public void deleteCacheById(ID id) {
        if (id == null) {
            return;
        }
        deleteCacheCascade(id);
    }

    public void deleteCacheByParentId(ID parentId) {
        if (parentIdField == null || parentId == null) {
            return;
        }
        removeEntriesByParentInternal(parentId);
    }

    @SuppressWarnings("unchecked")
    public void deleteCacheByParentIdUnchecked(Object parentId) {
        if (parentId == null) {
            return;
        }
        deleteCacheByParentId((ID) parentId);
    }

    @SuppressWarnings("unchecked")
    public void deleteCacheByIdUnchecked(Object id) {
        if (id == null) {
            return;
        }
        deleteCacheById((ID) id);
    }

    @SuppressWarnings("unchecked")
    private void handleChildCleanupBeforeDelete(List<T> entitiesToDelete) {
        if (entitiesToDelete == null || entitiesToDelete.isEmpty()) {
            return;
        }

        Map<String, AutoCacheRepository<?, ?, ?>> repositories =
                (Map<String, AutoCacheRepository<?, ?, ?>>) (Map<?, ?>) applicationContext.getBeansOfType(AutoCacheRepository.class);
        Class<T> entityClass = getEntityClass();

        for (T entity : entitiesToDelete) {
            ID parentId = extractEntityId(entity);
            if (parentId == null) {
                continue;
            }

            for (AutoCacheRepository<?, ?, ?> repository : repositories.values()) {
                if (repository.parentEntityClass == null) {
                    continue;
                }
                if (!repository.parentEntityClass.isAssignableFrom(entityClass)) {
                    continue;
                }
                repository.syncToDatabaseByParentIdInternal(parentId);
                repository.removeEntriesByParentInternal(parentId);
            }
        }
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

    @SuppressWarnings("null")
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
                    if (dtoId != null) {
                        String redisKey = getRedisKey(dtoId);
                        getRedisTemplate().opsForValue().set(redisKey, Objects.requireNonNull(updated));
                    }
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
            parentIdField.set(dto, newParentId);
            return dto;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("DTO 부모 ID 업데이트 실패", e);
        }
    }

    @SuppressWarnings("null")
    private DTO saveToDatabase(DTO dto) {
        JpaRepository<T, ID> repository = getJpaRepository();
        T entity = convertToEntity(dto);

        ID previousId = extractEntityId(entity);
        boolean hasPersistentId = previousId != null && !isTemporaryId(previousId);

        if (!hasPersistentId) {
            setEntityId(entity, null);
        }

        T entityToSave = entity;
        if (hasPersistentId) {
            ID persistedId = Objects.requireNonNull(previousId);
            if (repository.existsById(persistedId)) {
                T origin = repository.findById(persistedId).orElse(null);
                if (origin != null) {
                    mergeEntityFields(origin, entity);
                    entityToSave = origin;
                }
            }
        }

        T savedEntity = repository.save(Objects.requireNonNull(entityToSave));

        DTO updatedDto = convertToDto(savedEntity);
        ID cacheId = extractId(updatedDto);

        if (cacheId != null) {
            String cacheKey = getRedisKey(cacheId);
            DTO dtoToCache = Objects.requireNonNull(updatedDto);
            getRedisTemplate().opsForValue().set(cacheKey, dtoToCache);
        }

        // 새로 영속화된 ID를 모든 하위 캐시에 전파
        if (isTemporaryId(previousId) && !isTemporaryId(cacheId)) {
            propagateParentIdChange(previousId, cacheId);
        }
        if (previousId != null && !Objects.equals(previousId, cacheId)) {
            String staleKey = getRedisKey(previousId);
            getRedisTemplate().delete(staleKey);
        }
        return updatedDto;
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
    public void deleteEntitiesNotInCache(Object parentId, Set<Object> persistentIds) {
        if (parentIdField == null || parentId == null) {
            return;
        }
        if (!parentIdField.getType().isInstance(parentId)) {
            return;
        }

        ID typedParentId = (ID) parentId;
        List<T> persistedEntities = loadEntitiesByParentId(typedParentId);
        if (persistedEntities == null || persistedEntities.isEmpty()) {
            return;
        }

        Set<ID> allowedIds = persistentIds == null ? Collections.emptySet()
                : persistentIds.stream()
                        .filter(Objects::nonNull)
                        .filter(id -> entityIdField.getType().isInstance(id))
                        .map(id -> (ID) id)
                        .collect(Collectors.toSet());

        List<T> targets = persistedEntities.stream()
                .filter(entity -> {
                    ID entityId = extractEntityId(entity);
                    return entityId != null && !allowedIds.contains(entityId);
                })
                .collect(Collectors.toList());

        if (targets.isEmpty()) {
            return;
        }

        handleChildCleanupBeforeDelete(targets);
        getJpaRepository().deleteAll(targets);
    }

    @SuppressWarnings("unchecked")
    public DTO syncToDatabaseByDtoUnchecked(Object dto) {
        return syncToDatabaseByDto((DTO) dto);
    }

    /**
     * 캐시에 존재하는 ParentId 하위 DTO들을 DB와 동기화하며, 캐시에 없어진 엔티티는 DB에서도 삭제합니다.
     */
    public List<DTO> syncToDatabaseByParentId(ID parentId) {
        if (parentIdField == null) {
            throw new UnsupportedOperationException("ParentId 필드가 없습니다.");
        }
        if (parentId == null) {
            return Collections.emptyList();
        }
        if (parentId instanceof Number number && number.longValue() < 0L) {
            return Collections.emptyList(); // 아직 영속화되지 않은 부모
        }

        List<DTO> cachedDtos = findDtoListByParentId(parentId);
        if (!cachedDtos.isEmpty()) {
            cachedDtos.forEach(this::syncToDatabaseByDto);
        }

        List<DTO> refreshedDtos = findDtoListByParentId(parentId);
        Set<ID> cachedPersistentIds = refreshedDtos.stream()
                .map(this::extractId)
                .filter(Objects::nonNull)
                .filter(id -> !isTemporaryId(id))
                .collect(Collectors.toSet());

        List<T> persistedEntities = loadEntitiesByParentId(parentId);
        if (persistedEntities == null || persistedEntities.isEmpty()) {
            return Collections.emptyList();
        }

        List<T> entitiesToDelete = persistedEntities.stream()
                .filter(entity -> {
                    ID entityId = extractEntityId(entity);
                    return entityId != null && !cachedPersistentIds.contains(entityId);
                })
                .collect(Collectors.toList());

        if (!entitiesToDelete.isEmpty()) {
            handleChildCleanupBeforeDelete(entitiesToDelete);
            getJpaRepository().deleteAll(entitiesToDelete);
        }
        return refreshedDtos;
    }

    @SuppressWarnings("unchecked")
    public List<DTO> syncToDatabaseByParentIdUnchecked(Object parentId) {
        return syncToDatabaseByParentId((ID) parentId);
    }

    public DTO syncToDatabaseByDto(DTO dto) {
        if (dto == null) {
            return null;
        }
        // 부모가 없을 때
        if (parentIdField == null) {
            return saveToDatabase(dto);
        }
        // 부모가 있을 때
        Object parentIdValue = getParentIdValue(dto);
        if (parentIdValue instanceof Number number && number.longValue() <0) {
            System.out.println("부모키가 음수라 저장할 수 없습니다");
            return null;
        }
        return saveToDatabase(dto);
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

    @SuppressWarnings("unchecked")
    private void syncToDatabaseByParentIdInternal(Object parentIdObject) {
        if (parentIdField == null || parentIdObject == null) {
            return;
        }
        if (!parentIdField.getType().isInstance(parentIdObject)) {
            return;
        }

        ID parentId = (ID) parentIdObject;
        syncToDatabaseByParentId(parentId);
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

    public boolean isPersistentId(Object id) {
        return id != null && !isTemporaryId(id);
    }

}