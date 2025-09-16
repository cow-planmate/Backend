package com.example.planmate.domain.redis.cache;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.planmate.domain.plan.entity.PlaceCategory;
import com.example.planmate.domain.plan.repository.PlaceCategoryRepository;
import com.example.planmate.domain.webSocket.lazydto.PlaceCategoryDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlaceCategoryCacheService {
    private final PlaceCategoryRepository placeCategoryRepository;
    private final RedisTemplate<String, PlaceCategoryDto> placeCategoryRedis;
    private static final String PLACECATEGORY_PREFIX = "PLACECATEGORY";

    public void warmupAll() {
        List<PlaceCategory> categories = placeCategoryRepository.findAll();
        for (PlaceCategory pc : categories) {
            placeCategoryRedis.opsForValue().set(PLACECATEGORY_PREFIX + pc.getPlaceCategoryId(), PlaceCategoryDto.fromEntity(pc));
        }
    }

    public PlaceCategory getPlaceCategory(int placeCategoryId) {
        PlaceCategoryDto dto = placeCategoryRedis.opsForValue().get(PLACECATEGORY_PREFIX + placeCategoryId);
        if (dto == null) return placeCategoryRepository.getReferenceById(placeCategoryId);
        return dto.toEntity();
    }
}
