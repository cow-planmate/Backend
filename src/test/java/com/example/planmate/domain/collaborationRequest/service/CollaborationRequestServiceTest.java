package com.example.planmate.domain.collaborationRequest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.planmate.domain.collaborationRequest.auth.CollaborationRequestValidator;
import com.example.planmate.domain.collaborationRequest.dto.InviteUserToPlanResponse;
import com.example.planmate.domain.collaborationRequest.entity.CollaborationRequest;
import com.example.planmate.domain.collaborationRequest.repository.CollaborationRequestRepository;
import com.example.planmate.domain.plan.auth.PlanAccessValidator;
import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.repository.PlanEditorRepository;
import com.example.planmate.domain.plan.repository.PlanRepository;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CollaborationRequestServiceTest {

    @Mock
    private PlanRepository planRepository;
    @Mock
    private PlanAccessValidator planAccessValidator;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CollaborationRequestRepository collaborationRequestRepository;
    @Mock
    private PlanEditorRepository planEditorRepository;
    @Mock
    private CollaborationRequestValidator collaborationRequestValidator;

    @InjectMocks
    private CollaborationRequestService collaborationRequestService;

    @Test
    @DisplayName("inviteUserToPlan: 사용자를 플랜에 성공적으로 초대한다.")
    void inviteUserToPlan_success() {
        // given
        UUID senderId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        String receiverNickname = "receiver1";

        Plan plan = mock(Plan.class);
        User sender = mock(User.class);
        User receiver = mock(User.class);

        given(receiver.getUserId()).willReturn(UUID.randomUUID());

        given(planAccessValidator.validateUserHasAccessToPlan(senderId, planId)).willReturn(plan);
        given(userRepository.findById(senderId)).willReturn(Optional.of(sender));
        given(userRepository.findByNickname(receiverNickname)).willReturn(Optional.of(receiver));

        given(planEditorRepository.existsByUserAndPlan(receiver, plan)).willReturn(false);
        given(collaborationRequestRepository.findBySenderAndReceiverAndPlanAndTypeAndStatus(any(), any(), any(), any(),
                any()))
                .willReturn(Optional.empty());

        // when
        InviteUserToPlanResponse response = collaborationRequestService.inviteUserToPlan(senderId, planId,
                receiverNickname);

        // then
        assertEquals("성공적으로 초대 메세지를 보냈습니다.", response.getMessage());
        verify(collaborationRequestRepository).save(any(CollaborationRequest.class));
    }
}
