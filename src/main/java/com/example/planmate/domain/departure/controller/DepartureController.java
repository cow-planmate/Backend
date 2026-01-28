package com.example.planmate.domain.departure.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.domain.departure.dto.SearchDepartureRepuest;
import com.example.planmate.domain.departure.dto.SearchDepartureResponse;
import com.example.planmate.domain.departure.service.DepartureService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Departure", description = "출발지 검색 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/departure")
public class DepartureController {
    private final DepartureService departureService;

    @Operation(summary = "출발지 검색", description = "입력한 키워드를 기반으로 출발 가능한 장소/도시 목록을 검색합니다.")
    @PostMapping("")
    public ResponseEntity<SearchDepartureResponse> searchDeparture(@RequestBody SearchDepartureRepuest repuest) throws IOException {
        SearchDepartureResponse response = departureService.searchDeparture(repuest.getDepartureQuery());
        return ResponseEntity.ok(response);
    }
}
