package com.sharedsync.framework.shared.framework.sync;

import com.sharedsync.framework.shared.framework.repository.AutoCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CacheSyncService {
    private final List<AutoCacheRepository<?, ?, ?>> cacheRepositories;

    @Transactional
    public void syncToDatabase(int rootId) {
        AutoCacheRepository<?, ?, ?> rootRepository = cacheRepositories.stream()
                .filter(repo -> !repo.isParentIdFieldPresent())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("루트 DTO를 가진 AutoCacheRepository를 찾을 수 없습니다."));

        Object persistedRootId = syncRecursively(rootRepository, rootId);
        clearCacheHierarchy(rootRepository, persistedRootId);
    }

    private Object syncRecursively(AutoCacheRepository<?, ?, ?> repository, Object id) {
        if (repository == null || id == null) {
            return null;
        }

        Object parentIdForChildren = id;

        Object dto = repository.findDtoByIdUnchecked(id);
        if (dto != null) {
            Object updatedDto = repository.syncToDatabaseByDtoUnchecked(dto);
            Object updatedId = repository.extractIdUnchecked(updatedDto);
            if (updatedId != null) {
                parentIdForChildren = updatedId;
            }
        }

        Object finalParentId = parentIdForChildren;
        Map<AutoCacheRepository<?, ?, ?>, List<?>> childDtos = cacheRepositories.stream()
                .filter(childRepo -> childRepo != repository)
                .filter(childRepo -> childRepo.isParentEntityOf(repository.getEntityType()))
                .collect(Collectors.toMap(childRepo -> childRepo,
                        childRepo -> childRepo.findDtoListByParentIdUnchecked(finalParentId)));

        for (Map.Entry<AutoCacheRepository<?, ?, ?>, List<?>> entry : childDtos.entrySet()) {
            AutoCacheRepository<?, ?, ?> childRepo = entry.getKey();

            List<?> dtos = entry.getValue();
            if (dtos == null) {
                dtos = List.of();
            }

            dtos.stream()
                    .filter(Objects::nonNull)
                    .forEach(childRepo::syncToDatabaseByDtoUnchecked);

            List<?> refreshed = childRepo.findDtoListByParentIdUnchecked(finalParentId);
            if (refreshed == null) {
                refreshed = List.of();
            }
            Set<Object> persistentIds = refreshed.stream()
                    .map(childRepo::extractIdUnchecked)
                    .filter(Objects::nonNull)
                    .filter(childRepo::isPersistentId)
                    .collect(Collectors.toSet());

            childRepo.deleteEntitiesNotInCache(finalParentId, persistentIds);
            persistentIds.forEach(childId -> syncRecursively(childRepo, childId));
        }

        return parentIdForChildren;
    }

    private void clearCacheHierarchy(AutoCacheRepository<?, ?, ?> repository, Object id) {
        if (repository == null || id == null) {
            return;
        }

        repository.deleteCacheByIdUnchecked(id);
    }
}