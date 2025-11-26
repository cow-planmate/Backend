package com.sharedsync.framework.shared.presence.storage;

import java.util.Map;

public interface PresenceStorage {
    boolean hasTracker(int rootId);
    void insertTracker(int rootId, String sessionId, int userId, String index);
    void removeTracker(int rootId, String sessionId, int userId);
    Map<Integer, Integer> getTrackerEntries(int rootId);

    void saveUserNickname(int userId, String nickname);
    String getNicknameByUserId(int userId);
    Integer getUserIdByNickname(String nickname);

    void mapUserToRoot(int rootId, int userId);
    int getRootIdByUserId(int userId);
    int removeUserRootMapping(int userId);
}
