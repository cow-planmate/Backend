package com.example.planmate.domain.travel.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.planmate.common.externalAPI.GoogleMap;
import com.example.planmate.domain.travel.dto.GetTravelResponse;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.travel.repository.TravelRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelService {
    private final TravelRepository travelRepository;
    private final GoogleMap googleMap;

    @Transactional(readOnly = true)
    public GetTravelResponse getTravel() {
        GetTravelResponse response = new GetTravelResponse();

        travelRepository.findAll().forEach(travel -> {
            response.addTravel(
                    travel.getTravelId(),
                    travel.getTravelName(),
                    travel.getTravelCategory().getTravelCategoryId(),
                    travel.getTravelCategory().getTravelCategoryName());
        });

        return response;
    }

    public Map<String, Double> getOrInitializeLocation(String city) {

        Travel travel = travelRepository.findByTravelName(city)
                .orElseThrow(() -> new IllegalArgumentException("해당 도시 여행지를 찾을 수 없습니다."));

        // 1️⃣ 이미 좌표가 있는 경우
        if (travel.hasCoordinate()) {
            Map<String, Double> map = new HashMap<>();
            map.put("lat", travel.getLatitude());
            map.put("lng", travel.getLongitude());
            return map;
        }

        // 2️⃣ 좌표 없으면 Google 호출
        Map<String, Double> location = googleMap.getDestinationLocation(city);

        if (location == null) {
            return null;
        }

        double lat = location.get("lat");
        double lng = location.get("lng");

        // 3️⃣ DB 저장 (캐싱)
        travel.initializeCoordinate(lat, lng);
        travelRepository.save(travel);

        return location;
    }
}
