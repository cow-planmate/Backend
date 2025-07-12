package com.example.planmate.controller;

import com.example.planmate.dto.MakePlanRequest;
import com.example.planmate.dto.MakePlanResponse;
import com.example.planmate.service.MakePlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/plan")
public class PlanController {
    private final MakePlanService makePlanService;
    @PostMapping("")
    public ResponseEntity<MakePlanResponse> makePlan(Authentication authentication, @RequestBody MakePlanRequest makePlanRequest) {
        int userId = Integer.parseInt(authentication.getName());
        MakePlanResponse response = makePlanService.makeService(
                userId,
                makePlanRequest.getDeparture(),
                makePlanRequest.getTravelId(),
                makePlanRequest.getDates(),
                makePlanRequest.getAdultCount(),
                makePlanRequest.getChildCount()
        );
        return ResponseEntity.ok(response);
    }
}
