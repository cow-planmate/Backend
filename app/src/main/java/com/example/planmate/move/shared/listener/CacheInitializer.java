package com.example.planmate.move.shared.listener;

import com.example.planmate.move.shared.framework.dto.CacheDto;
import com.example.planmate.move.shared.framework.repository.AutoCacheRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CacheInitializer {

    private final ApplicationContext context;

    // 전체 AutoCacheRepository 저장
    private Map<Class<?>, AutoCacheRepository<?, ?, ?>> cacheMap;

    @PostConstruct
    public void init() {
        cacheMap = new HashMap<>();

        Map<String, AutoCacheRepository> repositories =
                context.getBeansOfType(AutoCacheRepository.class);

        for (AutoCacheRepository repo : repositories.values()) {
            cacheMap.put(repo.getEntityType(), repo);
        }
    }

    /**
     * 루트 엔티티 ID만 넣으면 전체 계층 캐시 자동 로딩
     */
    public void initializeHierarchy(int rootId) {
        AutoCacheRepository<?, ?, ?> rootRepo = findRootRepository();
        if (rootRepo == null) {
            throw new IllegalStateException("No root CacheEntity found");
        }

        loadRecursively(rootRepo, rootId);
    }

    /**
     * 재귀적으로 캐시 로딩
     */
    private void loadRecursively(AutoCacheRepository repo, int id) {

        // 1) 루트/부모 DTO 로드
        CacheDto<?> dto = repo.loadFromDatabaseById(id);
        if (dto == null) return;

        // Redis 저장
        repo.save(dto);

        // 2) 자식 탐색
        for (AutoCacheRepository<?, ?, ?> childRepo : cacheMap.values()) {

            // 자기 자신 제외 + parent 관계가 아닌 엔티티 제외
            if (childRepo == repo) continue;
            if (!childRepo.isParentEntityOf(repo.getEntityType())) continue;

            // 3) 자식 DTO 목록 로드
            List<? extends CacheDto<?>> children =
                    childRepo.loadFromDatabaseByParentIdUnchecked(id);

            // 4) 각각 캐싱 + 재귀 호출
            for (var childDto : children) {
                Object childId = childRepo.extractIdUnchecked(childDto);
                childRepo.saveUnchecked(childDto);
                loadRecursively(childRepo, (Integer) childId);
            }
        }
    }

    /**
     * ParentId 없는 엔티티 = 루트
     */
    private AutoCacheRepository<?, ?, ?> findRootRepository() {
        return cacheMap.values().stream()
                .filter(repo -> !repo.isParentIdFieldPresent())
                .findFirst()
                .orElse(null);
    }
}
