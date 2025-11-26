package com.sharedsync.shared.sync;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sharedsync.shared.repository.AutoCacheRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CacheSyncService {
    private final List<AutoCacheRepository<?, ?, ?>> cacheRepositories;

    public void syncToDatabase(String rootId) {
        AutoCacheRepository<?, ?, ?> rootRepository = cacheRepositories.stream()
                .filter(repo -> !repo.isParentIdFieldPresent())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("루트 DTO를 가진 AutoCacheRepository를 찾을 수 없습니다."));

        syncRecursively(rootRepository, rootId);
    }

    private void syncRecursively(AutoCacheRepository<?, ?, ?> repository, Object id) {
        if (repository == null || id == null) {
            return;
        }

        Object dto = repository.findDtoByIdUnchecked(id);
        if (dto != null) {
            repository.syncToDatabaseByDtoUnchecked(dto);
        }

        Map<AutoCacheRepository<?, ?, ?>, List<?>> childDtos = cacheRepositories.stream()
                .filter(childRepo -> childRepo != repository)
                .filter(childRepo -> childRepo.isParentEntityOf(repository.getEntityType()))
                .collect(Collectors.toMap(childRepo -> childRepo,
                        childRepo -> childRepo.findDtoListByParentIdUnchecked(id)));

        for (Map.Entry<AutoCacheRepository<?, ?, ?>, List<?>> entry : childDtos.entrySet()) {
            AutoCacheRepository<?, ?, ?> childRepo = entry.getKey();

            List<?> dtos = entry.getValue();
            if (dtos == null) {
                dtos = List.of();
            }

            dtos.stream()
                    .filter(Objects::nonNull)
                    .forEach(childRepo::syncToDatabaseByDtoUnchecked);

            List<?> refreshed = childRepo.findDtoListByParentIdUnchecked(id);
            if (refreshed == null) {
                refreshed = List.of();
            }
            Set<Object> persistentIds = refreshed.stream()
                    .map(childRepo::extractIdUnchecked)
                    .filter(Objects::nonNull)
                    .filter(childRepo::isPersistentId)
                    .collect(Collectors.toSet());

            childRepo.deleteEntitiesNotInCache(id, persistentIds);

            persistentIds.forEach(childId -> syncRecursively(childRepo, childId));
        }
    }

    // public void syncToDatabase(int planId) {
    //     PlanDto planDto = planCache.findDtoById(planId);
    //     PlanDto updatedPlanDto = planCache.syncToDatabaseByDto(planDto);

    //     timeTableCache.syncToDatabaseByParentId(updatedPlanDto.planId());
    //     List<TimeTableDto> refreshedTimeTables = timeTableCache.findDtoListByParentId(planId);

    //     for (TimeTableDto timeTableDto : refreshedTimeTables) {
    //         Integer timeTableId = timeTableDto.timeTableId();
    //         timeTablePlaceBlockCache.syncToDatabaseByParentId(timeTableId);
    //         timeTablePlaceBlockCache.deleteCacheByParentId(timeTableId);
    //     }
    //     timeTableCache.deleteCacheByParentId(planId);
    //     planCache.deleteCacheById(planId);
    // }
}