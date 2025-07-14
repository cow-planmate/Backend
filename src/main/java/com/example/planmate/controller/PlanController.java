package com.example.planmate.controller;

import com.example.planmate.dto.*;
import com.example.planmate.service.EditPlanNameService;
import com.example.planmate.service.GetPlanService;
import com.example.planmate.service.MakePlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/plan")
public class PlanController {
    private final MakePlanService makePlanService;
    private final GetPlanService getPlanService;
    private final EditPlanNameService editPlanNameService;
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
    @GetMapping("/{planId}")
    public ResponseEntity<GetPlanResponse> getPlan(Authentication authentication, @PathVariable int planId) throws AccessDeniedException {
        int userId = Integer.parseInt(authentication.getName());
        GetPlanResponse response = getPlanService.getPlan(userId, planId);
        return ResponseEntity.ok(response);
    }
    @PatchMapping("/{planId}/name")
    public ResponseEntity<EditPlanNameReponse> editPlanName(Authentication authentication, @PathVariable int planId, @RequestBody EditPlanNameRequest editPlanNameRequest) {
        int userId = Integer.parseInt(authentication.getName());
        EditPlanNameReponse reponse = editPlanNameService.EditPlanName(userId, planId, editPlanNameRequest.getPlanName());
        return ResponseEntity.ok(reponse);
    }
}
