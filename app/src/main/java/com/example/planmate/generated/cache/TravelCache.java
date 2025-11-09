package com.example.planmate.generated.cache;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.travel.repository.TravelCategoryRepository;
import com.example.planmate.domain.travel.repository.TravelRepository;
import com.example.planmate.generated.lazydto.TravelDto;
import com.sharedsync.framework.shared.framework.repository.AutoCacheRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TravelCache extends AutoCacheRepository<Travel, Integer, TravelDto> {

}
