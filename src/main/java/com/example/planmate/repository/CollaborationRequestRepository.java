package com.example.planmate.repository;

import com.example.planmate.entity.CollaborationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollaborationRequestRepository extends JpaRepository<CollaborationRequest, Integer> {

}