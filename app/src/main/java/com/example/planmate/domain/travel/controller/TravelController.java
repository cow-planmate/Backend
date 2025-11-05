package com.example.planmate.domain.travel.controller;

import com.example.planmate.domain.travel.dto.GetTravelResponse;
import com.example.planmate.domain.travel.service.TravelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/travel")
public class TravelController {
    private final TravelService travelService;
    @GetMapping("")
    public ResponseEntity<GetTravelResponse> getTravel() {
        GetTravelResponse response = travelService.getTravel();
        return ResponseEntity.ok(response);
    }
}
