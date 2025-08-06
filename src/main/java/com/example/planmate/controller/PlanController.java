package com.example.planmate.controller;

import com.example.planmate.dto.*;
import com.example.planmate.service.*;
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
    private final MakePlanService makePlanService;
    private final GetPlanService getPlanService;
    private final EditPlanNameService editPlanNameService;
    private final GetPlaceService getPlaceService;
    private final SavePlanService savePlanService;
    private final DeletePlanService deletePlanService;
    private final InviteUserToPlanService inviteUserToPlanService;
    private final RequestEditAccessService requestEditAccessService;
    private final ResignEditorAccessService resignEditorAccessService;
    private final RemoveEditorAccessByOwnerService removeEditorAccessByOwnerService;
    private final GetCompletePlanService getCompletePlanService;
    private final GetEditorsService getEditorsService;

    @PostMapping("")
    public ResponseEntity<MakePlanResponse> makePlan(Authentication authentication, @RequestBody MakePlanRequest makePlanRequest) {
        int userId = Integer.parseInt(authentication.getName());
        MakePlanResponse response = makePlanService.makeService(
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
        GetPlanResponse response = getPlanService.getPlan(userId, planId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{planId}/complete")
    public ResponseEntity<GetCompletePlanResponse> getCompletePlan(@PathVariable("planId") int planId) throws AccessDeniedException {
        GetCompletePlanResponse response = getCompletePlanService.getCompletePlan(planId);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{planId}")
    public ResponseEntity<DeletePlanResponse> deletePlan(Authentication authentication, @PathVariable("planId") int planId) throws AccessDeniedException {
        int userId = Integer.parseInt(authentication.getName());
        DeletePlanResponse response = deletePlanService.deletePlan(userId, planId);
        return ResponseEntity.ok(response);
    }
    @PatchMapping("/{planId}/save")
    public ResponseEntity<SavePlanResponse> savePlan(Authentication authentication, @PathVariable("planId") int planId, @RequestBody SavePlanRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        SavePlanResponse response = savePlanService.savePlan(userId, planId, request.getDeparture(), request.getTransportationCategoryId(), request.getAdultCount(), request.getChildCount(), request.getTimetables(), request.getTimetablePlaceBlocks());
        return ResponseEntity.ok(response);
    }
    @PatchMapping("/{planId}/name")
    public ResponseEntity<EditPlanNameResponse> editPlanName(Authentication authentication, @PathVariable("planId") int planId, @RequestBody EditPlanNameRequest editPlanNameRequest) {
        int userId = Integer.parseInt(authentication.getName());
        EditPlanNameResponse reponse = editPlanNameService.EditPlanName(userId, planId, editPlanNameRequest.getPlanName());
        return ResponseEntity.ok(reponse);
    }
    @PostMapping("/{planId}/lodging")
    public ResponseEntity<PlaceResponse> getLodgingPlace(Authentication authentication, @PathVariable("planId") int planId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        PlaceResponse response = getPlaceService.getLodgingPlace(userId, planId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{planId}/tour")
    public ResponseEntity<PlaceResponse> getTourPlace(Authentication authentication, @PathVariable("planId") int planId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        PlaceResponse response = getPlaceService.getTourPlace(userId, planId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{planId}/restaurant")
    public ResponseEntity<PlaceResponse> getRestaurantPlace(Authentication authentication, @PathVariable("planId") int planId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        PlaceResponse response = getPlaceService.getRestaurantPlace(userId, planId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{planId}/invite")
    public ResponseEntity<InviteUserToPlanResponse> inviteUserToPlan(Authentication authentication, @PathVariable("planId") int planId, @RequestBody InviteUserToPlanRequest request) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        InviteUserToPlanResponse response = inviteUserToPlanService.inviteUserToPlan(userId, planId, request.getReceiverNickname());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{planId}/request-access")
    public ResponseEntity<RequestEditAccessResponse> requestEditAccess(Authentication authentication, @PathVariable("planId") int planId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        RequestEditAccessResponse response = requestEditAccessService.requestEditAccess(userId, planId);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{planId}/editor/me")
    public ResponseEntity<ResignEditorAccessResponse> resignEditorAccess(Authentication authentication, @PathVariable("planId") int planId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        ResignEditorAccessResponse response = resignEditorAccessService.resignEditorAccess(userId, planId);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{planId}/editor/{targetUserId}")
    public ResponseEntity<RemoveEditorAccessByOwnerResponse> removeEditorAccessByOwner(Authentication authentication, @PathVariable("planId") int planId, @PathVariable("targetUserId") int targetUserId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        RemoveEditorAccessByOwnerResponse response = removeEditorAccessByOwnerService.removeEditorAccessByOwner(userId, planId, targetUserId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{planId}/editors")
    public ResponseEntity<GetEditorsResponse> getEditors(Authentication authentication, @PathVariable("planId") int planId) throws AccessDeniedException {
        int userId = Integer.parseInt(authentication.getName());
        GetEditorsResponse response = getEditorsService.getEditors(userId, planId);
        return ResponseEntity.ok(response);
    }
}
