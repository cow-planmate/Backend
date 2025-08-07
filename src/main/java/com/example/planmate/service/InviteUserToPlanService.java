package com.example.planmate.service;

import com.example.planmate.auth.PlanAccessValidator;
import com.example.planmate.dto.GetPlanResponse;
import com.example.planmate.dto.InviteUserToPlanResponse;
import com.example.planmate.entity.*;
import com.example.planmate.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InviteUserToPlanService {
    private final PlanAccessValidator planAccessValidator;
    private final UserRepository userRepository;
    private final CollaborationRequestRepository collaborationRequestRepository;
    private final PlanEditorRepository planEditorRepository;


    @Transactional
    public InviteUserToPlanResponse inviteUserToPlan(int senderId, int planId, String receiverNickname) {
        InviteUserToPlanResponse response = new InviteUserToPlanResponse();

        // 1. 사용자와 플랜 유효성 검증
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(senderId, planId);
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("보낸 유저가 존재하지 않습니다."));

        // 2. 닉네임으로 받는 유저 조회
        User receiver = userRepository.findByNickname(receiverNickname)
                .orElseThrow(() -> new IllegalArgumentException("해당 닉네임의 유저가 존재하지 않습니다."));

        if (planEditorRepository.existsByUserAndPlan(receiver, plan)) {
            throw new IllegalStateException("이미 편집 권한이 있는 유저입니다.");
        }

        // 3. 이미 초대한 적이 있는지 확인 (PENDING 상태)
        Optional<CollaborationRequest> existingRequest =
                collaborationRequestRepository.findBySenderAndReceiverAndPlanAndTypeAndStatus(
                        sender, receiver, plan, CollaborationRequestType.INVITE, CollaborationRequestStatus.PENDING
                );

        if (existingRequest.isPresent()) {
            throw new IllegalStateException("이미 초대한 유저입니다.");
        }

        // 4. CollaborationRequest 생성
        CollaborationRequest request = CollaborationRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .plan(plan)
                .type(CollaborationRequestType.INVITE)
                .status(CollaborationRequestStatus.PENDING)
                .build();

        collaborationRequestRepository.save(request);

        response.setMessage("성공적으로 초대 메세지를 보냈습니다.");

        return response;
    }
}
