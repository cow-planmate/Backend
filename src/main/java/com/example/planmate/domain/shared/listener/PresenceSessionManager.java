package com.example.planmate.domain.shared.listener;

import org.springframework.stereotype.Service;

import com.example.planmate.domain.shared.enums.EAction;
import com.example.planmate.domain.shared.service.PresenceTrackingService;
import com.example.planmate.domain.shared.sync.CacheSyncService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PresenceSessionManager {

    private static final int DEFAULT_DAY_INDEX = 0;

    private final PresenceTrackingService presenceTrackingService;
    private final CacheSyncService cacheSyncService;
    private final CacheInitializer planCacheInitializer;
    private final PresenceBroadcaster presenceBroadcaster;

    public void handleSubscribe(int planId, int userId) {
        if (!presenceTrackingService.hasPlanTracker(planId)) {
            planCacheInitializer.initializeHierarchy(planId);
        }

        presenceTrackingService.insertPlanTracker(planId, userId, DEFAULT_DAY_INDEX);
        presenceTrackingService.insertNickname(userId);
        presenceTrackingService.insertUserIdToPlanId(planId, userId);

        broadcast(planId, userId, EAction.CREATE);
    }

    public void handleDisconnect(int userId) {
        int planId = presenceTrackingService.removeUserIdToPlanId(userId);
        if (planId == 0) {
            return;
        }

        presenceTrackingService.removePlanTracker(planId, userId);
        presenceTrackingService.removeNickname(userId);

        if (!presenceTrackingService.hasPlanTracker(planId)) {
            cacheSyncService.syncToDatabase(planId);
        }

        broadcast(planId, userId, EAction.DELETE);
    }

    private void broadcast(int planId, int userId, EAction action) {
        String nickname = presenceTrackingService.getNicknameByUserId(userId);
        presenceBroadcaster.broadcast(planId, userId, nickname, action);
    }
}
