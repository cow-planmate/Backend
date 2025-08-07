package com.example.planmate.service;

import com.example.planmate.dto.GetEditorsResponse;
import com.example.planmate.dto.ResignEditorAccessResponse;
import com.example.planmate.entity.Plan;
import com.example.planmate.entity.PlanEditor;
import com.example.planmate.entity.User;
import com.example.planmate.repository.PlanEditorRepository;
import com.example.planmate.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetEditorsService {
    private final PlanEditorRepository planEditorRepository;
    private final PlanRepository planRepository;

    @Transactional(readOnly = true)
    public GetEditorsResponse getEditors(int userId, int planId) {
        GetEditorsResponse response = new GetEditorsResponse();

        if (!planRepository.existsByPlanIdAndUserUserId(planId, userId) &&
                !planEditorRepository.existsByUser_UserIdAndPlan_PlanId(userId, planId)) {
            throw new IllegalArgumentException("해당 일정에 대한 접근 권한이 없습니다.");
        }

        List<PlanEditor> editors = planEditorRepository.findByPlan_PlanId(planId);

        for (PlanEditor editor : editors) {
            User editorDetail = editor.getUser();
            response.addSimpleEditorVO(editorDetail.getUserId(), editorDetail.getNickname());
        }

        response.setMessage("성공적으로 편집자 목록을 가져왔습니다.");
        return response;
    }
}
