package com.sharedsync.shared.presence.storage;

import java.util.Map;

public interface PresenceStorage {
    boolean hasTracker(String rootId);
    void insertTracker(String rootId, String sessionId, String userId, String index);
    void removeTracker(String rootId, String sessionId, String userId);
    Map<String, String> getTrackerEntries(String rootId);

    void saveUserNickname(String userId, String nickname);
    String getNicknameByUserId(String userId);
    String getUserIdByNickname(String nickname);

    void mapUserToRoot(String rootId, String userId);
    String getRootIdByUserId(String userId);
    String removeUserRootMapping(String userId);
}
