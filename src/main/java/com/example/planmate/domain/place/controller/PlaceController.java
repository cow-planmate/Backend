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

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/plan")
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping("/{planId}/lodging")
    public ResponseEntity<PlaceResponse> getLodgingPlace(Authentication authentication, @PathVariable("planId") int planId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        PlaceResponse response = placeService.getLodgingPlace(userId, planId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{planId}/tour")
    public ResponseEntity<PlaceResponse> getTourPlace(Authentication authentication, @PathVariable("planId") int planId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        PlaceResponse response = placeService.getTourPlace(userId, planId);
        return ResponseEntity.ok(response); 
    }

    @GetMapping("/{planId}/restaurant")
    public ResponseEntity<PlaceResponse> getRestaurantPlace(Authentication authentication, @PathVariable("planId") int planId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        PlaceResponse response = placeService.getRestaurantPlace(userId, planId);
        return ResponseEntity.ok(response);
    }

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

    @GetMapping("/lodging/{travelCategoryName}/{travelName}")
    public ResponseEntity<PlaceResponse> getLodgingPlace(
        @PathVariable("travelCategoryName") String travelCategoryName,
        @PathVariable("travelName") String travelName
    ) throws IOException {
        PlaceResponse response = placeService.getLodgingPlace(travelCategoryName, travelName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tour/{travelCategoryName}/{travelName}")
    public ResponseEntity<PlaceResponse> getTourPlace(
        @PathVariable("travelCategoryName") String travelCategoryName,
        @PathVariable("travelName") String travelName
    ) throws IOException {
        PlaceResponse response = placeService.getTourPlace(travelCategoryName, travelName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/restaurant/{travelCategoryName}/{travelName}")
    public ResponseEntity<PlaceResponse> getRestaurantPlace(
        @PathVariable("travelCategoryName") String travelCategoryName,
        @PathVariable("travelName") String travelName
    ) throws IOException {
        PlaceResponse response = placeService.getRestaurantPlace(travelCategoryName, travelName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/place/{query}")
    public ResponseEntity<PlaceResponse> getPlace(@PathVariable("query") String query) throws IOException {
        PlaceResponse response = placeService.getSearchPlace(query);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/nextplace")
    public ResponseEntity<PlaceResponse> getNextPlace(@RequestBody NextPlaceRequest request) throws IOException {
        PlaceResponse response = placeService.getNextPlace(request);
        return ResponseEntity.ok(response);
    }
}
