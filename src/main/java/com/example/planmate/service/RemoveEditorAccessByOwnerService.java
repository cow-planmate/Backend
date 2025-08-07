package com.example.planmate.service;

import com.example.planmate.dto.RemoveEditorAccessByOwnerResponse;
import com.example.planmate.dto.ResignEditorAccessResponse;
import com.example.planmate.entity.Plan;
import com.example.planmate.entity.PlanEditor;
import com.example.planmate.repository.PlanEditorRepository;
import com.example.planmate.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveEditorAccessByOwnerService {
    private final PlanEditorRepository planEditorRepository;
    private final PlanRepository planRepository;

    @Transactional
    public RemoveEditorAccessByOwnerResponse removeEditorAccessByOwner(int ownerId, int planId, int targetUserId) {
        RemoveEditorAccessByOwnerResponse response = new RemoveEditorAccessByOwnerResponse();

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("플랜이 존재하지 않습니다."));

        if (!plan.getUser().getUserId().equals(ownerId)) {
            throw new SecurityException("플랜 소유자만 편집 권한을 삭제할 수 있습니다.");
        }

        PlanEditor planEditor = planEditorRepository.findByUser_UserIdAndPlan_PlanId(targetUserId, planId)
                .orElseThrow(() -> new IllegalArgumentException("해당 편집 권한이 존재하지 않습니다."));

        planEditorRepository.delete(planEditor);

        response.setMessage("성공적으로 편집 권한을 삭제하였습니다");
        return response;
    }
}
