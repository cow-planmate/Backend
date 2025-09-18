package com.example.planmate.domain.webSocket.service;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.planmate.domain.plan.entity.PlaceCategory;
import com.example.planmate.domain.plan.repository.PlaceCategoryRepository;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.travel.repository.TravelCategoryRepository;
import com.example.planmate.domain.travel.repository.TravelRepository;
import com.example.planmate.domain.webSocket.lazydto.PlaceCategoryDto;
import com.example.planmate.domain.webSocket.lazydto.TravelDto;
// Removed external cache service imports after refactor

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final TravelRepository travelRepository;
    // Removed plan, timetable, and place block Redis templates (moved to dedicated cache services)
    private final RedisTemplate<String, TravelDto> travelRedis;
    private final String TRAVEL_PREFIX = "TRAVEL";
    private final RedisTemplate<String, PlaceCategoryDto> placeCategoryRedis;
    private final String PLACECATEGORY_PREFIX = "PLACECATEGORY";
    private final PlaceCategoryRepository placeCategoryRepository;
    private final TravelCategoryRepository travelCategoryRepository;

    @PostConstruct
    public void init() {
        List<Travel> travels = travelRepository.findAll();
        for(Travel travel : travels) {
            travelRedis.opsForValue().set(TRAVEL_PREFIX + travel.getTravelId(), TravelDto.fromEntity(travel));
        }
        List<PlaceCategory> placeCategories = placeCategoryRepository.findAll();
        for(PlaceCategory placeCategory : placeCategories) {
            placeCategoryRedis.opsForValue().set(PLACECATEGORY_PREFIX + placeCategory.getPlaceCategoryId(), PlaceCategoryDto.fromEntity(placeCategory));
        }
    }



    public Travel getTravelByTravelId(int travelId) {
        TravelDto dto = travelRedis.opsForValue().get(TRAVEL_PREFIX + travelId);
        if (dto == null) return null;
        return dto.toEntity(travelCategoryRepository.getReferenceById(dto.travelCategoryId()));
    }

    public PlaceCategory getPlaceCategory(int placeCategoryId) {
        PlaceCategoryDto dto = placeCategoryRedis.opsForValue().get(PLACECATEGORY_PREFIX + placeCategoryId);
        if (dto == null) return placeCategoryRepository.getReferenceById(placeCategoryId);
        return dto.toEntity();
    }
}
