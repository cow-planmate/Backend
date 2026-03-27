package com.example.planmate.domain.travel.service;

import java.util.HashMap;
import java.util.Map;

import com.example.planmate.domain.travel.entity.TravelCategory;
import com.example.planmate.domain.travel.repository.TravelCategoryRepository;
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
    private final TravelCategoryRepository travelCategoryRepository;
    private final GoogleMap googleMap;

    @Transactional(readOnly = true)
    public GetTravelResponse getTravel() {
        GetTravelResponse response = new GetTravelResponse();

        travelRepository.findAllWithCategory().forEach(travel -> {
            response.addTravel(
                    travel.getTravelId(),
                    travel.getTravelName(),
                    travel.getTravelCategory().getTravelCategoryId(),
                    travel.getTravelCategory().getTravelCategoryName());
        });

        return response;
    }

    @Transactional
    public Map<String, Double> getOrInitializeLocation(String city) {
        Travel travel = getTravelFromCityString(city);

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

    @Transactional(readOnly = true)
    public Travel getTravelFromCityString(String city) {

        if (city == null || !city.contains(" ")) {
            throw new IllegalArgumentException("카테고리명과 여행지명이 함께 전달되어야 합니다.");
        }

        // 1️⃣ 첫 공백 기준으로 분리
        String categoryName = city.substring(0, city.indexOf(" "));
        String travelName = city.substring(city.indexOf(" ") + 1);

        // 2️⃣ 카테고리 조회
        TravelCategory category = travelCategoryRepository
                .findByTravelCategoryName(categoryName)
                .orElseThrow(() ->
                        new IllegalArgumentException("해당 카테고리를 찾을 수 없습니다.")
                );

        // 3️⃣ 카테고리 + 여행지명으로 조회
        return travelRepository
                .findByTravelNameAndTravelCategory(travelName, category)
                .orElseThrow(() ->
                        new IllegalArgumentException("해당 여행지를 찾을 수 없습니다.")
                );
    }
}
