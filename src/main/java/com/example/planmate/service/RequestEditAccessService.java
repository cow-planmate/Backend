package com.example.planmate.service;

import com.example.planmate.auth.PlanAccessValidator;
import com.example.planmate.dto.InviteUserToPlanResponse;
import com.example.planmate.dto.RequestEditAccessResponse;
import com.example.planmate.entity.*;
import com.example.planmate.repository.CollaborationRequestRepository;
import com.example.planmate.repository.PlanEditorRepository;
import com.example.planmate.repository.PlanRepository;
import com.example.planmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RequestEditAccessService {
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final CollaborationRequestRepository collaborationRequestRepository;
    private final PlanEditorRepository planEditorRepository;


    @Transactional
    public RequestEditAccessResponse requestEditAccess(int senderId, int planId) {
        RequestEditAccessResponse response = new RequestEditAccessResponse();

        // 1. 플랜 조회
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new IllegalArgumentException("요청한 일정이 존재하지 않습니다."));

        // 2. 유저 조회
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("요청한 유저가 존재하지 않습니다."));

        // 3. 플랜의 작성자(owner)가 존재하는지 확인
        User owner = plan.getUser();
        if (owner == null) {
            throw new IllegalStateException("플랜의 소유자가 존재하지 않습니다.");
        }

        // 4. 본인이 본인에게 요청하지 못하도록 막기
        if (sender.getUserId().equals(owner.getUserId())) {
            throw new IllegalArgumentException("자신에게는 권한 요청을 보낼 수 없습니다.");
        }

        if (planEditorRepository.existsByUserAndPlan(sender, plan)) {
            throw new IllegalStateException("이미 편집 권한이 있는 유저입니다.");
        }

        // 5. 이미 권한 요청 보냈는지 확인 (PENDING 상태)
        Optional<CollaborationRequest> existingRequest =
                collaborationRequestRepository.findBySenderAndReceiverAndPlanAndTypeAndStatus(
                        sender, owner, plan, CollaborationRequestType.REQUEST, CollaborationRequestStatus.PENDING
                );

        if (existingRequest.isPresent()) {
            throw new IllegalStateException("이미 권한 요청을 보낸 상태입니다.");
        }

        // 6. CollaborationRequest 생성 및 저장
        CollaborationRequest request = CollaborationRequest.builder()
                .sender(sender)
                .receiver(owner)
                .plan(plan)
                .type(CollaborationRequestType.REQUEST)
                .status(CollaborationRequestStatus.PENDING)
                .build();

        collaborationRequestRepository.save(request);

        response.setMessage("성공적으로 권한 요청을 보냈습니다.");

        return response;
    }
}
