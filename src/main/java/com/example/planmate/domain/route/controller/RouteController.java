package com.example.planmate.domain.route.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.domain.route.dto.RouteRequest;
import com.example.planmate.domain.route.dto.RouteResponse;
import com.example.planmate.domain.route.dto.RouteTableRequest;
import com.example.planmate.domain.route.dto.RouteTableResponse;
import com.example.planmate.domain.route.dto.RouteTripRequest;
import com.example.planmate.domain.route.dto.RouteTripResponse;
import com.example.planmate.domain.route.dto.TransitRouteRequest;
import com.example.planmate.domain.route.dto.TransitRouteResponse;
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

    @Operation(summary = "장소 쌍별 소요시간/거리 매트릭스 조회", description = "좌표 목록의 모든 쌍에 대한 소요 시간(초)과 이동 거리(m) 매트릭스를 반환합니다.")
    @PostMapping("/table")
    public RouteTableResponse getTable(@RequestBody RouteTableRequest request) {
        return routeService.getTable(request.getWaypoints(), request.getProfile());
    }

    @Operation(summary = "방문 순서 최적화", description = "첫 좌표를 출발지로 고정하고 나머지 좌표의 최적 방문 순서를 계산합니다.")
    @PostMapping("/trip")
    public RouteTripResponse getTrip(@RequestBody RouteTripRequest request) {
        return routeService.getTrip(request.getWaypoints(), request.getProfile(), request.isRoundtrip());
    }

    @Operation(summary = "대중교통 경로 조회", description = "두 지점 간 대중교통(지하철/버스) 최적 경로의 소요 시간과 요금을 반환합니다.")
    @PostMapping("/transit")
    public TransitRouteResponse getTransit(@RequestBody TransitRouteRequest request) {
        return routeService.getTransit(request.getFrom(), request.getTo());
    }
}
