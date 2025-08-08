package com.example.planmate.domain.plan.controller;

import com.example.planmate.domain.plan.dto.*;
import com.example.planmate.domain.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/plan")
public class PlanController {
    private final PlanService planService;

    @PostMapping("")
    public ResponseEntity<MakePlanResponse> makePlan(Authentication authentication, @RequestBody MakePlanRequest makePlanRequest) {
        int userId = Integer.parseInt(authentication.getName());
        MakePlanResponse response = planService.makeService(
                userId,
                makePlanRequest.getDeparture(),
                makePlanRequest.getTravelId(),
                makePlanRequest.getTransportationCategoryId(),
                makePlanRequest.getDates(),
                makePlanRequest.getAdultCount(),
                makePlanRequest.getChildCount()
        );
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{planId}")
    public ResponseEntity<GetPlanResponse> getPlan(Authentication authentication, @PathVariable("planId") int planId) throws AccessDeniedException {
        int userId = Integer.parseInt(authentication.getName());
        GetPlanResponse response = planService.getPlan(userId, planId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{planId}/complete")
    public ResponseEntity<GetCompletePlanResponse> getCompletePlan(@PathVariable("planId") int planId) throws AccessDeniedException {
        GetCompletePlanResponse response = planService.getCompletePlan(planId);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{planId}")
    public ResponseEntity<DeletePlanResponse> deletePlan(Authentication authentication, @PathVariable("planId") int planId) throws AccessDeniedException {
        int userId = Integer.parseInt(authentication.getName());
        DeletePlanResponse response = planService.deletePlan(userId, planId);
        return ResponseEntity.ok(response);
    }
    @PatchMapping("/{planId}/save")
    public ResponseEntity<SavePlanResponse> savePlan(Authentication authentication, @PathVariable("planId") int planId, @RequestBody SavePlanRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        SavePlanResponse response = planService.savePlan(userId, planId, request.getDeparture(), request.getTransportationCategoryId(), request.getAdultCount(), request.getChildCount(), request.getTimetables(), request.getTimetablePlaceBlocks());
        return ResponseEntity.ok(response);
    }
    @PatchMapping("/{planId}/name")
    public ResponseEntity<EditPlanNameResponse> editPlanName(Authentication authentication, @PathVariable("planId") int planId, @RequestBody EditPlanNameRequest editPlanNameRequest) {
        int userId = Integer.parseInt(authentication.getName());
        EditPlanNameResponse reponse = planService.EditPlanName(userId, planId, editPlanNameRequest.getPlanName());
        return ResponseEntity.ok(reponse);
    }
    @PostMapping("/{planId}/lodging")
    public ResponseEntity<PlaceResponse> getLodgingPlace(Authentication authentication, @PathVariable("planId") int planId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        PlaceResponse response = planService.getLodgingPlace(userId, planId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{planId}/tour")
    public ResponseEntity<PlaceResponse> getTourPlace(Authentication authentication, @PathVariable("planId") int planId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        PlaceResponse response = planService.getTourPlace(userId, planId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{planId}/restaurant")
    public ResponseEntity<PlaceResponse> getRestaurantPlace(Authentication authentication, @PathVariable("planId") int planId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        PlaceResponse response = planService.getRestaurantPlace(userId, planId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{planId}/place")
    public ResponseEntity<PlaceResponse> getPlace(Authentication authentication, @PathVariable("planId") int planId, @RequestBody SearchPlaceRequest request) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        String query = request.getQuery();
        PlaceResponse response = planService.getSearchPlace(userId, planId, query);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{planId}/editor/me")
    public ResponseEntity<ResignEditorAccessResponse> resignEditorAccess(Authentication authentication, @PathVariable("planId") int planId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        ResignEditorAccessResponse response = planService.resignEditorAccess(userId, planId);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{planId}/editor/{targetUserId}")
    public ResponseEntity<RemoveEditorAccessByOwnerResponse> removeEditorAccessByOwner(Authentication authentication, @PathVariable("planId") int planId, @PathVariable("targetUserId") int targetUserId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        RemoveEditorAccessByOwnerResponse response = planService.removeEditorAccessByOwner(userId, planId, targetUserId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{planId}/editors")
    public ResponseEntity<GetEditorsResponse> getEditors(Authentication authentication, @PathVariable("planId") int planId) throws AccessDeniedException {
        int userId = Integer.parseInt(authentication.getName());
        GetEditorsResponse response = planService.getEditors(userId, planId);
        return ResponseEntity.ok(response);
    }

}
