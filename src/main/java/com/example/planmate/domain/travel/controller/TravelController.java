package com.example.planmate.domain.travel.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.domain.travel.dto.GetTravelResponse;
import com.example.planmate.domain.travel.service.TravelService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Travel", description = "여행지(도시/지역) 정보 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/travel")
public class TravelController {
    private final TravelService travelService;

    @Operation(summary = "여행지 목록 조회", description = "시스템에서 지원하는 모든 여행지(도시 및 카테고리) 목록을 조회합니다.")
    @GetMapping("")
    public ResponseEntity<GetTravelResponse> getTravel() {
        GetTravelResponse response = travelService.getTravel();
        return ResponseEntity.ok(response);
    }
}
