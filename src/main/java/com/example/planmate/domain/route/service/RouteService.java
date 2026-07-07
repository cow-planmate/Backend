package com.example.planmate.domain.route.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.planmate.common.externalAPI.OsrmRouting;
import com.example.planmate.common.externalAPI.OsrmRouting.RoutingResult;
import com.example.planmate.domain.route.dto.RoutePointDto;
import com.example.planmate.domain.route.dto.RouteResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final OsrmRouting osrmRouting;

    public RouteResponse getRoute(List<RoutePointDto> waypoints) {
        if (waypoints == null || waypoints.isEmpty()) {
            return new RouteResponse(new ArrayList<>(), 0, 0);
        }

        RoutingResult result = osrmRouting.getRoute(waypoints);

        if (result != null) {
            return new RouteResponse(result.path(), result.distance(), result.duration());
        }

        // 도로 경로를 찾지 못하면 입력 좌표를 그대로 돌려줘 프론트에서 직선으로 대체한다.
        return new RouteResponse(new ArrayList<>(waypoints), 0, 0);
    }
}
