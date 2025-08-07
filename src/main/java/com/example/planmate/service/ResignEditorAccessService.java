package com.example.planmate.service;

import com.example.planmate.dto.ResignEditorAccessResponse;
import com.example.planmate.entity.PlanEditor;
import com.example.planmate.repository.PlanEditorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResignEditorAccessService {
    private final PlanEditorRepository planEditorRepository;

    @Transactional
    public ResignEditorAccessResponse resignEditorAccess(int userId, int planId) {
        ResignEditorAccessResponse response = new ResignEditorAccessResponse();

        PlanEditor planEditor = planEditorRepository.findByUser_UserIdAndPlan_PlanId(userId, planId).orElseThrow(() -> new IllegalArgumentException("해당 편집 권한이 존재하지 않습니다."));

        planEditorRepository.delete(planEditor);

        response.setMessage("성공적으로 편집 권한을 삭제하였습니다");
        return response;
    }
}
