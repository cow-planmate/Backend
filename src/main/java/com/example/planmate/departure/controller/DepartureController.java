package com.example.planmate.departure.controller;

import com.example.planmate.departure.dto.SearchDepartureRepuest;
import com.example.planmate.departure.dto.SearchDepartureResponse;
import com.example.planmate.departure.service.DepartureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/departure")
public class DepartureController {
    private final DepartureService departureService;
    @PostMapping("")
    public ResponseEntity<SearchDepartureResponse> searchDeparture(@RequestBody SearchDepartureRepuest repuest) throws IOException {
        SearchDepartureResponse response = departureService.searchDeparture(repuest.getDepartureQuery());
        return ResponseEntity.ok(response);
    }
}
