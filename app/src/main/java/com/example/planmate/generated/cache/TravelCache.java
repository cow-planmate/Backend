package com.example.planmate.generated.cache;

import org.springframework.stereotype.Component;

import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.generated.lazydto.TravelDto;
import com.sharedsync.framework.shared.framework.repository.CacheRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TravelCache extends CacheRepository<Travel, Integer, TravelDto> {

}
