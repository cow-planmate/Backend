package com.example.planmate.generated.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.example.planmate.domain.plan.entity.PlaceCategory;
import com.example.planmate.domain.plan.repository.PlaceCategoryRepository;
import com.sharedsync.framework.shared.enums.ECasheKey;
import com.example.planmate.generated.lazydto.PlaceCategoryDto;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class PlaceCategoryCache {
    private final RedisTemplate<String, PlaceCategoryDto> placeCategoryRedis;
    private final PlaceCategoryRepository placeCategoryRepository;

    @PostConstruct
    public void init() {
        placeCategoryRepository.findAll().forEach(pc -> 
            placeCategoryRedis.opsForValue().set(ECasheKey.PLACECATEGORY.key(pc.getPlaceCategoryId()), PlaceCategoryDto.fromEntity(pc))
        );
    }

    public PlaceCategory findPlaceCategoryByPlaceCategoryId(int placeCategoryId) {
        PlaceCategoryDto dto = placeCategoryRedis.opsForValue().get(ECasheKey.PLACECATEGORY.key(placeCategoryId));
        if (dto == null) return placeCategoryRepository.getReferenceById(placeCategoryId);
        return dto.toEntity();
    }
}
