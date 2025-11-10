package com.example.planmate.generated.listener;

import org.springframework.stereotype.Service;

import com.example.planmate.generated.service.PresenceTrackingService;
import com.sharedsync.framework.shared.sync.CacheSyncService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PresenceSessionManager {

    private static final int DEFAULT_DAY_INDEX = 0;
    private static final String ACTION_CREATE = "create";
    private static final String ACTION_DELETE = "delete";

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

        broadcast(planId, userId, ACTION_CREATE);
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

        broadcast(planId, userId, ACTION_DELETE);
    }

    private void broadcast(int planId, int userId, String action) {
        String nickname = presenceTrackingService.getNicknameByUserId(userId);
        presenceBroadcaster.broadcast(planId, userId, nickname, action);
    }
}
