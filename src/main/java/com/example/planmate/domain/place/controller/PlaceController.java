package com.example.planmate.domain.place.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.domain.place.dto.NextPlaceRequest;
import com.example.planmate.domain.place.dto.PlaceResponse;
import com.example.planmate.domain.place.service.PlaceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Place", description = "장소 추천 및 검색 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/plan")
public class PlaceController {

    private final PlaceService placeService;

    @Operation(summary = "숙소 추천 조회", description = "플랜 ID를 바탕으로 해당 지역의 숙소(lodging) 추천 목록을 가져옵니다.")
    @GetMapping("/{planId}/lodging")
    public ResponseEntity<PlaceResponse> getLodgingPlace(Authentication authentication, @PathVariable("planId") int planId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        PlaceResponse response = placeService.getLodgingPlace(userId, planId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "관광지 추천 조회", description = "플랜 ID를 바탕으로 해당 지역의 관광지(tour) 추천 목록을 가져옵니다.")
    @GetMapping("/{planId}/tour")
    public ResponseEntity<PlaceResponse> getTourPlace(Authentication authentication, @PathVariable("planId") int planId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        PlaceResponse response = placeService.getTourPlace(userId, planId);
        return ResponseEntity.ok(response); 
    }

    @Operation(summary = "음식점 추천 조회", description = "플랜 ID를 바탕으로 해당 지역의 음식점(restaurant) 추천 목록을 가져옵니다.")
    @GetMapping("/{planId}/restaurant")
    public ResponseEntity<PlaceResponse> getRestaurantPlace(Authentication authentication, @PathVariable("planId") int planId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        PlaceResponse response = placeService.getRestaurantPlace(userId, planId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "장소 키워드 검색", description = "플랜 내에서 특정 키워드로 장소를 검색합니다.")
    @GetMapping("/{planId}/place/{query}")
    public ResponseEntity<PlaceResponse> getPlace(
        Authentication authentication,
        @PathVariable("planId") int planId,
        @PathVariable("query") String query
    ) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        PlaceResponse response = placeService.getSearchPlace(userId, planId, query);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카테고리별 숙소 조회", description = "여행 지역 카테고리와 이름을 기반으로 숙소를 조회합니다.")
    @GetMapping("/lodging/{travelCategoryName}/{travelName}")
    public ResponseEntity<PlaceResponse> getLodgingPlace(
        @PathVariable("travelCategoryName") String travelCategoryName,
        @PathVariable("travelName") String travelName
    ) throws IOException {
        PlaceResponse response = placeService.getLodgingPlace(travelCategoryName, travelName);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카테고리별 관광지 조회", description = "여행 지역 카테고리와 이름을 기반으로 관광지를 조회합니다.")
    @GetMapping("/tour/{travelCategoryName}/{travelName}")
    public ResponseEntity<PlaceResponse> getTourPlace(
        @PathVariable("travelCategoryName") String travelCategoryName,
        @PathVariable("travelName") String travelName
    ) throws IOException {
        PlaceResponse response = placeService.getTourPlace(travelCategoryName, travelName);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카테고리별 음식점 조회", description = "여행 지역 카테고리와 이름을 기반으로 음식점을 조회합니다.")
    @GetMapping("/restaurant/{travelCategoryName}/{travelName}")
    public ResponseEntity<PlaceResponse> getRestaurantPlace(
        @PathVariable("travelCategoryName") String travelCategoryName,
        @PathVariable("travelName") String travelName
    ) throws IOException {
        PlaceResponse response = placeService.getRestaurantPlace(travelCategoryName, travelName);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "일반 장소 검색", description = "플랜 정보 없이 키워드만으로 장소를 검색합니다.")
    @GetMapping("/place/{query}")
    public ResponseEntity<PlaceResponse> getPlace(@PathVariable("query") String query) throws IOException {
        PlaceResponse response = placeService.getSearchPlace(query);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "검색 결과 다음 페이지 조회", description = "검색 결과의 다음 페이지(pagination) 정보를 가져옵니다.")
    @PostMapping("/nextplace")
    public ResponseEntity<PlaceResponse> getNextPlace(@RequestBody NextPlaceRequest request) throws IOException {
        PlaceResponse response = placeService.getNextPlace(request);
        return ResponseEntity.ok(response);
    }
}
