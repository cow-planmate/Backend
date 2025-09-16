package com.example.planmate.domain.redis.cache;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.travel.repository.TravelCategoryRepository;
import com.example.planmate.domain.travel.repository.TravelRepository;
import com.example.planmate.domain.webSocket.lazydto.TravelDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelCacheService {
    private final TravelRepository travelRepository;
    private final TravelCategoryRepository travelCategoryRepository;
    private final RedisTemplate<String, TravelDto> travelRedis;
    private static final String TRAVEL_PREFIX = "TRAVEL";

    public void warmupAll() {
        List<Travel> travels = travelRepository.findAll();
        for (Travel travel : travels) {
            travelRedis.opsForValue().set(TRAVEL_PREFIX + travel.getTravelId(), TravelDto.fromEntity(travel));
        }
    }

    public Travel getTravelByTravelId(int travelId) {
        TravelDto dto = travelRedis.opsForValue().get(TRAVEL_PREFIX + travelId);
        if (dto == null) return null;
    return dto.toEntity(travelCategoryRepository.getReferenceById(dto.travelCategoryId()));
    }
}
