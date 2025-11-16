package com.example.planmate.move.shared.listener;

import com.example.planmate.move.shared.presence.core.PresenceBroadcaster;
import com.example.planmate.move.shared.presence.core.PresenceRootResolver;
import com.example.planmate.move.shared.presence.core.UserProvider;
import com.example.planmate.move.shared.presence.storage.PresenceStorage;
import com.example.planmate.move.shared.sync.CacheSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PresenceSessionManager {

    private static final int DEFAULT_DAY_INDEX = 0;
    private static final String ACTION_CREATE = "create";
    private static final String ACTION_DELETE = "delete";

    private final PresenceStorage presenceStorage;
    private final PresenceBroadcaster presenceBroadcaster;
    private final UserProvider userProvider;        // ✅ 새 의존성
    private final CacheInitializer planCacheInitializer;
    private final CacheSyncService cacheSyncService;
    private final PresenceRootResolver presenceRootResolver;

    /**
     * STOMP 구독 시 (입장)
     */
    public void handleSubscribe(int rootId, int userId) {
        if (!presenceStorage.hasTracker(rootId)) {
            planCacheInitializer.initializeHierarchy(rootId);
        }

        String nickname = presenceStorage.getNicknameByUserId(userId);
        if (nickname == null || nickname.isBlank()) {
            nickname = userProvider.findNicknameByUserId(userId);
            if (nickname != null) presenceStorage.saveUserNickname(userId, nickname);
        }

        presenceStorage.insertTracker(rootId, userId, DEFAULT_DAY_INDEX);
        presenceStorage.mapUserToRoot(rootId, userId);

        String channel = presenceRootResolver.getChannel();

        String finalNickname = nickname;
        presenceBroadcaster.broadcast(
                channel,
                rootId,
                ACTION_CREATE,
                new Object() {
                    public final String userNickname = finalNickname;
                    public final int uid = userId;
                }
        );
    }


    /**
     * 연결 해제 시 (퇴장)
     */
    public void handleDisconnect(int userId) {
        int rootId = presenceStorage.removeUserRootMapping(userId);
        if (rootId == 0) return;

        presenceStorage.removeTracker(rootId, userId);

        if (!presenceStorage.hasTracker(rootId)) {
            cacheSyncService.syncToDatabase(rootId);
        }

        String channel = presenceRootResolver.getChannel();
        String nickname = presenceStorage.getNicknameByUserId(userId);

        presenceBroadcaster.broadcast(
                channel,
                rootId,
                ACTION_DELETE,
                new Object() {
                    public final String userNickname = nickname;
                    public final int uid = userId;
                }
        );
    }

}
