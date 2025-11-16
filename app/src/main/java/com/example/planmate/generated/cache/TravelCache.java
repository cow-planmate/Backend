package com.example.planmate.generated.cache;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.travel.repository.TravelCategoryRepository;
import com.example.planmate.domain.travel.repository.TravelRepository;
import com.example.planmate.generated.lazydto.TravelDto;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TravelCache {
    private static final String TRAVEL_KEY_PREFIX = "TRAVEL";

    private final RedisTemplate<String, TravelDto> travelRedis;
    private final TravelRepository travelRepository;
    private final TravelCategoryRepository travelCategoryRepository;
        
    @PostConstruct
    public void init() {
        List<Travel> travels = travelRepository.findAll();
        for(Travel travel : travels) {
            travelRedis.opsForValue().set(travelKey(travel.getTravelId()), TravelDto.fromEntity(travel));
        }
    }
//    public Travel findTravelByTravelId(int travelId) {
//        TravelDto dto = travelRedis.opsForValue().get(travelKey(travelId));
//        if (dto == null) return null;
//        return dto.toEntity(travelCategoryRepository.getReferenceById(dto.travelCategoryId()));
//    }

    private static String travelKey(int travelId) {
        return TRAVEL_KEY_PREFIX + travelId;
    }
}
