package com.example.planmate.domain.collaborationRequest.repository;

import com.example.planmate.domain.collaborationRequest.entity.CollaborationRequest;
import com.example.planmate.domain.collaborationRequest.enums.CollaborationRequestStatus;
import com.example.planmate.domain.collaborationRequest.enums.CollaborationRequestType;
import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollaborationRequestRepository extends JpaRepository<CollaborationRequest, Integer> {

    Optional<CollaborationRequest> findBySenderAndReceiverAndPlanAndTypeAndStatus(User sender, User receiver, Plan plan, CollaborationRequestType collaborationRequestType, CollaborationRequestStatus collaborationRequestStatus);

    List<CollaborationRequest> findByReceiver_UserIdAndStatus(int receiverId, CollaborationRequestStatus collaborationRequestStatus);
}