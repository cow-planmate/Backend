package com.sharedsync.shared.listener;

import org.springframework.stereotype.Service;

import com.sharedsync.shared.presence.core.PresenceBroadcaster;
import com.sharedsync.shared.presence.core.PresenceRootResolver;
import com.sharedsync.shared.presence.core.UserProvider;
import com.sharedsync.shared.presence.storage.PresenceStorage;
import com.sharedsync.shared.sync.CacheSyncService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PresenceSessionManager {

    private static final String DEFAULT_INDEX = "0";
    private static final String ACTION_CREATE = "create";
    private static final String ACTION_DELETE = "delete";

    private final PresenceStorage presenceStorage;
    private final PresenceBroadcaster presenceBroadcaster;
    private final UserProvider userProvider;        
    private final CacheInitializer planCacheInitializer;
    private final CacheSyncService cacheSyncService;
    private final PresenceRootResolver presenceRootResolver;

    /**
     * STOMP 구독 시 (입장)
     */
    public void handleSubscribe(int rootId, int userId, String sessionId) {
        if (!presenceStorage.hasTracker(rootId)) {
            planCacheInitializer.initializeHierarchy(rootId);
        }

        String nickname = presenceStorage.getNicknameByUserId(userId);
        if (nickname == null || nickname.isBlank()) {
            nickname = userProvider.findNicknameByUserId(userId);
            if (nickname != null) presenceStorage.saveUserNickname(userId, nickname);
        }

        presenceStorage.insertTracker(rootId, sessionId, userId, DEFAULT_INDEX);
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
    public void handleDisconnect(int userId, String sessionId) {
        int rootId = presenceStorage.removeUserRootMapping(userId);
        if (rootId == 0) return;

        presenceStorage.removeTracker(rootId, sessionId, userId);

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
