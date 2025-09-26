package com.example.planmate.domain.shared.sync;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.shared.realtime.meta.RelationshipInspector;
import com.example.planmate.domain.shared.sync.framework.PlanSyncContext;
import com.example.planmate.domain.shared.sync.framework.SyncPipeline;
import com.example.planmate.domain.shared.sync.framework.SyncStep;
import com.example.planmate.domain.shared.sync.framework.TargetedSyncStep;
import com.example.planmate.domain.shared.sync.framework.steps.CleanupCacheStep;
import com.example.planmate.domain.shared.sync.framework.steps.SyncPlanStep;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheSyncService {

    private final SyncPlanStep syncPlanStep; // still injected; also available via targetedSteps registry
    private final List<TargetedSyncStep> targetedSteps;
    private final CleanupCacheStep cleanupRedisStep;
    private final RelationshipInspector relationshipInspector;

    @Transactional
    public void syncPlanToDatabase(int planId) {
        PlanSyncContext ctx = new PlanSyncContext(planId);
        SyncPipeline<PlanSyncContext> pipeline = buildRelationDrivenPipeline();
        pipeline.run(ctx);
    }

    private SyncPipeline<PlanSyncContext> buildRelationDrivenPipeline() {
        SyncPipeline<PlanSyncContext> pipeline = new SyncPipeline<>();

        Map<Class<?>, SyncStep<PlanSyncContext>> registry = new HashMap<>();
        // candidates: all relation types from Plan (including Plan itself)
        Set<Class<?>> candidates = collectRelationTypes(Plan.class);
        // auto-register all targeted steps by their target entity type (annotation or name-based)
        for (TargetedSyncStep step : targetedSteps) {
            Class<?> target = null;
            try {
                target = step.targetEntity();
            } catch (IllegalStateException ignored) {
            }
            if (target == null) {
                target = inferTargetClassByName(step.getClass().getSimpleName(), candidates);
            }
        }

        // add root plan step from registry to keep a single registration path
        SyncStep<PlanSyncContext> root = registry.get(Plan.class);
        if (root == null) {
            // fallback for safety: use injected syncPlanStep
            root = syncPlanStep;
        }
        pipeline.add(root);

        Set<Class<?>> visited = new HashSet<>();
        addChildSteps(pipeline, Plan.class, registry, visited);

        pipeline.add(cleanupRedisStep);
        return pipeline;
    }

    private void addChildSteps(SyncPipeline<PlanSyncContext> pipeline,
                               Class<?> parent,
                               Map<Class<?>, SyncStep<PlanSyncContext>> registry,
                               Set<Class<?>> visited) {
        relationshipInspector.inspect(parent).values().forEach(meta -> {
            if (!meta.collection()) return; // 자식(컬렉션)만 내려감
            Class<?> child = meta.relatedClass();
            if (visited.add(child)) {
                SyncStep<PlanSyncContext> step = registry.get(child);
                if (step != null) pipeline.add(step);
                // 깊이 우선으로 더 내려감
                addChildSteps(pipeline, child, registry, visited);
            }
        });
    }

    private Set<Class<?>> collectRelationTypes(Class<?> root) {
        Set<Class<?>> result = new HashSet<>();
        Deque<Class<?>> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Class<?> cur = stack.pop();
            if (result.add(cur)) {
                relationshipInspector.inspect(cur).values().forEach(meta -> {
                    if (meta.collection()) stack.push(meta.relatedClass());
                });
            }
        }
        return result;
    }

    private Class<?> inferTargetClassByName(String stepSimpleName, Set<Class<?>> candidates) {
        // Strip common prefixes/suffixes: Sync + (EntityName) + Step
        String base = stepSimpleName;
        if (base.startsWith("Sync")) base = base.substring(4);
        if (base.endsWith("Step")) base = base.substring(0, base.length() - 4);
        if (base.endsWith("SyncStep")) base = base.substring(0, base.length() - 8);
        for (Class<?> c : candidates) {
            if (c.getSimpleName().equals(base)) return c;
        }
        return null;
    }
}