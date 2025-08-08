package com.example.planmate.collaborationRequest.repository;

import com.example.planmate.collaborationRequest.entity.CollaborationRequest;
import com.example.planmate.collaborationRequest.entity.CollaborationRequestStatus;
import com.example.planmate.collaborationRequest.entity.CollaborationRequestType;
import com.example.planmate.plan.entity.Plan;
import com.example.planmate.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollaborationRequestRepository extends JpaRepository<CollaborationRequest, Integer> {

    Optional<CollaborationRequest> findBySenderAndReceiverAndPlanAndTypeAndStatus(User sender, User receiver, Plan plan, CollaborationRequestType collaborationRequestType, CollaborationRequestStatus collaborationRequestStatus);

    List<CollaborationRequest> findByReceiver_UserIdAndStatus(int receiverId, CollaborationRequestStatus collaborationRequestStatus);
}