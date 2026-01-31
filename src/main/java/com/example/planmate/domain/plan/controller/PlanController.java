package com.example.planmate.domain.plan.controller;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.common.exception.UnauthorizedException;
import com.example.planmate.domain.collaborationRequest.dto.InviteUserToPlanRequest;
import com.example.planmate.domain.collaborationRequest.dto.InviteUserToPlanResponse;
import com.example.planmate.domain.collaborationRequest.dto.RequestEditAccessResponse;
import com.example.planmate.domain.collaborationRequest.service.CollaborationRequestService;
import com.example.planmate.domain.plan.auth.PlanAccessValidator;
import com.example.planmate.domain.plan.dto.CreatePlanRequest;
import com.example.planmate.domain.plan.dto.CreatePlanResponse;
import com.example.planmate.domain.plan.dto.DeleteMultiplePlansRequest;
import com.example.planmate.domain.plan.dto.DeleteMultiplePlansResponse;
import com.example.planmate.domain.plan.dto.DeletePlanResponse;
import com.example.planmate.domain.plan.dto.EditPlanNameRequest;
import com.example.planmate.domain.plan.dto.EditPlanNameResponse;
import com.example.planmate.domain.plan.dto.GetCompletePlanResponse;
import com.example.planmate.domain.plan.dto.GetEditorsResponse;
import com.example.planmate.domain.plan.dto.GetPlanResponse;
import com.example.planmate.domain.plan.dto.GetShareLinkResponse;
import com.example.planmate.domain.plan.dto.MakePlanRequest;
import com.example.planmate.domain.plan.dto.MakePlanResponse;
import com.example.planmate.domain.plan.dto.RemoveEditorAccessByOwnerResponse;
import com.example.planmate.domain.plan.dto.ResignEditorAccessResponse;
import com.example.planmate.domain.plan.service.PlanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Plan", description = "여행 플랜(일정) 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/plan")
public class PlanController {
    private final PlanService planService;
    private final CollaborationRequestService collaborationRequestService;
    private final PlanAccessValidator planAccessValidator;

    @Operation(summary = "플랜 초기 생성", description = "여행 출발지, 목적지, 날짜 등을 입력하여 새로운 플랜을 처음 생성합니다.")
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

    @Operation(summary = "플랜 상세 조회", description = "특정 플랜의 상세 정보(프레임 및 기초 정보)를 조회합니다.")
    @GetMapping("/{planId}")
    public ResponseEntity<GetPlanResponse> getPlan(Authentication authentication, @PathVariable("planId") int planId) throws AccessDeniedException {
        int userId = Integer.parseInt(authentication.getName());
        GetPlanResponse response = planService.getPlan(userId, planId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "완료된 플랜 조회", description = "완료 페이지에서 사용하는 플랜 상세 데이터를 조회합니다. 공유 토큰이 있으면 비로그인 유저도 접근 가능합니다.")
    @GetMapping("/{planId}/complete")
    public ResponseEntity<GetCompletePlanResponse> getCompletePlan(
            Authentication authentication,
            @PathVariable("planId") int planId,
            @RequestParam(value = "token", required = false) String shareToken
    ) {
        GetCompletePlanResponse response;
        if (shareToken != null && !shareToken.isBlank()) {
            // 토큰이 있는 경우 → 인증 없이 처리
            planService.validateShareToken(planId, shareToken);
        } else {
            // 토큰이 없는 경우 → 인증 필수
            if (authentication == null) {
                throw new UnauthorizedException("로그인이 필요합니다.");
            }
            int userId = Integer.parseInt(authentication.getName());
            planAccessValidator.checkUserAccessToPlan(userId, planId);
        }
        response = planService.getCompletePlan(planId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "플랜 삭제", description = "특정 ID의 여행 플랜을 삭제합니다.")
    @DeleteMapping("/{planId}")
    public ResponseEntity<DeletePlanResponse> deletePlan(Authentication authentication, @PathVariable("planId") int planId) throws AccessDeniedException {
        int userId = Integer.parseInt(authentication.getName());
        DeletePlanResponse response = planService.deletePlan(userId, planId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "플랜 전체 생성/수정", description = "프레임, 타임테이블, 장소 블록을 포함한 전체 플랜 데이터를 한 번에 생성 또는 업데이트합니다.")
    @PostMapping("/create")
    public ResponseEntity<CreatePlanResponse> createPlan(Authentication authentication, @RequestBody CreatePlanRequest request) {
        int userId = Integer.parseInt(authentication.getName());
        CreatePlanResponse response = planService.createPlan(
                userId,
                request.getPlanFrame().getDeparture(),
                request.getPlanFrame().getTravelId(),
                request.getPlanFrame().getTransportationCategoryId(),
                request.getPlanFrame().getAdultCount(),
                request.getPlanFrame().getChildCount(),
                request.getTimetables(),
                request.getTimetablePlaceBlocks()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "여러 플랜 일괄 삭제", description = "선택한 여러 개의 여행 플랜을 한 번에 삭제합니다.")
    @DeleteMapping("")
    public ResponseEntity<DeleteMultiplePlansResponse> deleteMultiplePlans(Authentication authentication, @RequestBody DeleteMultiplePlansRequest request) throws AccessDeniedException {
        int userId = Integer.parseInt(authentication.getName());
        DeleteMultiplePlansResponse response = planService.deleteMultiplePlans(userId, request.getPlanIds());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "플랜 이름 수정", description = "여행 플랜의 제목(이름)을 변경합니다.")
    @PatchMapping("/{planId}/name")
    public ResponseEntity<EditPlanNameResponse> editPlanName(Authentication authentication, @PathVariable("planId") int planId, @RequestBody EditPlanNameRequest editPlanNameRequest) {
        int userId = Integer.parseInt(authentication.getName());
        EditPlanNameResponse reponse = planService.EditPlanName(userId, planId, editPlanNameRequest.getPlanName());
        return ResponseEntity.ok(reponse);
    }

    @Operation(summary = "본인 편집 권한 포기", description = "초대받은 플랜에서 본인의 편집 권한을 포기하고 목록에서 제거합니다.")
    @DeleteMapping("/{planId}/editor/me")
    public ResponseEntity<ResignEditorAccessResponse> resignEditorAccess(Authentication authentication, @PathVariable("planId") int planId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        ResignEditorAccessResponse response = planService.resignEditorAccess(userId, planId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "편집자 권한 박탈", description = "플랜 소유자가 특정 편집자의 권한을 제거합니다.")
    @DeleteMapping("/{planId}/editors/{targetUserId}")
    public ResponseEntity<RemoveEditorAccessByOwnerResponse> removeEditorAccessByOwner(Authentication authentication, @PathVariable("planId") int planId, @PathVariable("targetUserId") int targetUserId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        RemoveEditorAccessByOwnerResponse response = planService.removeEditorAccessByOwner(userId, planId, targetUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "편집자 목록 조회", description = "플랜에 참여 중인 모든 공동 편집자의 목록을 조회합니다.")
    @GetMapping("/{planId}/editors")
    public ResponseEntity<GetEditorsResponse> getEditors(Authentication authentication, @PathVariable("planId") int planId) throws AccessDeniedException {
        int userId = Integer.parseInt(authentication.getName());
        GetEditorsResponse response = planService.getEditors(userId, planId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "사용자 초대", description = "닉네임을 사용하여 다른 사용자를 해당 플랜의 편집자로 초대합니다.")
    @PostMapping("/{planId}/invite")
    public ResponseEntity<InviteUserToPlanResponse> inviteUserToPlan(Authentication authentication, @PathVariable("planId") int planId, @RequestBody InviteUserToPlanRequest request) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        InviteUserToPlanResponse response = collaborationRequestService.inviteUserToPlan(userId, planId, request.getReceiverNickname());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "편집 권한 요청", description = "해당 플랜의 소유자에게 편집 권한을 요청합니다.")
    @PostMapping("/{planId}/request-access")
    public ResponseEntity<RequestEditAccessResponse> requestEditAccess(Authentication authentication, @PathVariable("planId") int planId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        RequestEditAccessResponse response = collaborationRequestService.requestEditAccess(userId, planId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "공유 링크 생성/조회", description = "다른 사람에게 공유할 수 있는 플랜 뷰어 전용 URL을 생성하거나 조회합니다.")
    @GetMapping("/{planId}/share")
    public ResponseEntity<GetShareLinkResponse> getShareLink(Authentication authentication, @PathVariable("planId") int planId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        GetShareLinkResponse response = planService.getShareLink(userId, planId);
        return ResponseEntity.ok(response);
    }

}
