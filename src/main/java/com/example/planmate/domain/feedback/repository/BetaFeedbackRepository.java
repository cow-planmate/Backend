package com.example.planmate.domain.feedback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.planmate.domain.feedback.entity.BetaFeedback;

public interface BetaFeedbackRepository extends JpaRepository<BetaFeedback, Long> {
}
