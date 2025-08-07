package com.example.planmate.service;

import com.example.planmate.repository.PlanEditorRepository;
import com.example.planmate.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetEditorsService {
    private final PlanEditorRepository planEditorRepository;
    private final PlanRepository planRepository;


}
