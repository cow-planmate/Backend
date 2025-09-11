package com.example.planmate.domain.plan.controller;

import com.example.planmate.domain.collaborationRequest.dto.InviteUserToPlanRequest;
import com.example.planmate.domain.collaborationRequest.dto.InviteUserToPlanResponse;
import com.example.planmate.domain.collaborationRequest.dto.RequestEditAccessResponse;
import com.example.planmate.domain.collaborationRequest.service.CollaborationRequestService;
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
    private final CollaborationRequestService collaborationRequestService;

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
    @PatchMapping("/save")
    public ResponseEntity<SavePlanResponse> savePlan(Authentication authentication, @RequestBody SavePlanRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        SavePlanResponse response = planService.savePlan(userId, request.getDeparture(), request.getTravelId(), request.getTransportationCategoryId(), request.getAdultCount(), request.getChildCount(), request.getTimetables(), request.getTimetablePlaceBlocks());
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("")
    public ResponseEntity<DeleteMultiplePlansResponse> deleteMultiplePlans(Authentication authentication, @RequestBody DeleteMultiplePlansRequest request) throws AccessDeniedException {
        int userId = Integer.parseInt(authentication.getName());
        DeleteMultiplePlansResponse response = planService.deleteMultiplePlans(userId, request.getPlanIds());
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



    @PostMapping("/lodging")
    public ResponseEntity<PlaceResponse> getLodgingPlace(@RequestBody PlaceRequest request) throws IOException {
        PlaceResponse response = planService.getLodgingPlace(request.getTravelCategoryName(), request.getTravelName());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/tour")
    public ResponseEntity<PlaceResponse> getTourPlace(@RequestBody PlaceRequest request) throws IOException {
        PlaceResponse response = planService.getTourPlace(request.getTravelCategoryName(), request.getTravelName());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/restaurant")
    public ResponseEntity<PlaceResponse> getRestaurantPlace(@RequestBody PlaceRequest request) throws IOException {
        PlaceResponse response = planService.getRestaurantPlace(request.getTravelCategoryName(), request.getTravelName());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/place")
    public ResponseEntity<PlaceResponse> getPlace(@RequestBody SearchPlaceRequest request) throws IOException {
        PlaceResponse response = planService.getSearchPlace(request.getQuery());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/nextplace")
    public ResponseEntity<PlaceResponse> getNextPlace(@RequestBody NextPlaceRequest request) throws IOException {
        PlaceResponse response = planService.getNextPlace(request.getNextPageTokens());
        return ResponseEntity.ok(response);
    }



    @DeleteMapping("/{planId}/editor/me")
    public ResponseEntity<ResignEditorAccessResponse> resignEditorAccess(Authentication authentication, @PathVariable("planId") int planId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        ResignEditorAccessResponse response = planService.resignEditorAccess(userId, planId);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{planId}/editors/{targetUserId}")
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
    @PostMapping("/{planId}/invite")
    public ResponseEntity<InviteUserToPlanResponse> inviteUserToPlan(Authentication authentication, @PathVariable("planId") int planId, @RequestBody InviteUserToPlanRequest request) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        InviteUserToPlanResponse response = collaborationRequestService.inviteUserToPlan(userId, planId, request.getReceiverNickname());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{planId}/request-access")
    public ResponseEntity<RequestEditAccessResponse> requestEditAccess(Authentication authentication, @PathVariable("planId") int planId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        RequestEditAccessResponse response = collaborationRequestService.requestEditAccess(userId, planId);
        return ResponseEntity.ok(response);
    }

}
