package com.example.planmate.generated.cache;

import org.springframework.stereotype.Component;

import com.example.planmate.domain.plan.entity.PlaceCategory;
import com.example.planmate.generated.lazydto.PlaceCategoryDto;
import com.sharedsync.framework.shared.framework.repository.CacheRepository;

import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class PlaceCategoryCache extends CacheRepository<PlaceCategory, Integer, PlaceCategoryDto> {
}
