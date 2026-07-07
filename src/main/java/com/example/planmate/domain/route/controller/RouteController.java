package com.example.planmate.domain.route.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.domain.route.dto.RouteRequest;
import com.example.planmate.domain.route.dto.RouteResponse;
import com.example.planmate.domain.route.service.RouteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Route", description = "길찾기(경로) 관련 API")
@RestController
@RequestMapping("/api/route")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @Operation(summary = "도로 기반 경로 조회", description = "방문 순서대로 나열된 좌표 목록을 도로를 따라가는 경로로 변환합니다.")
    @PostMapping("/directions")
    public RouteResponse getDirections(@RequestBody RouteRequest request) {
        return routeService.getRoute(request.getWaypoints());
    }
}
