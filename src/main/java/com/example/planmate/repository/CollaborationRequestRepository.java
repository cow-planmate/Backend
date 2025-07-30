package com.example.planmate.repository;

import com.example.planmate.entity.CollaborationRequest;
import com.example.planmate.entity.CollaborationRequestStatus;
import com.example.planmate.entity.CollaborationRequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollaborationRequestRepository extends JpaRepository<CollaborationRequest, Integer> {
    List<CollaborationRequest> findByStatusAndReceiverId(CollaborationRequestStatus status, Integer receiverId);
}