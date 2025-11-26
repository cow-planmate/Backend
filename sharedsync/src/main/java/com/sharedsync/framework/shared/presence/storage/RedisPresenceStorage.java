package com.sharedsync.framework.shared.presence.storage;

import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisPresenceStorage implements PresenceStorage {

    private final RedisTemplate<String, Object> redis;

    private static final String TRACKER = "PRESENCE:TRACKER:";
    private static final String USER_TO_ROOT = "PRESENCE:USER_ROOT:";
    private static final String NICKNAME = "PRESENCE:NICKNAME:";
    private static final String NAME_TO_ID = "PRESENCE:NAME_ID:";

    @Override
    public boolean hasTracker(int rootId) {
        return redis.hasKey(TRACKER + rootId);
    }

    @Override
    public void insertTracker(int rootId, String sessionId, int userId, String index) {
        redis.opsForHash().put(TRACKER + rootId, String.valueOf(userId + "//" + sessionId), index);
    }

    @Override
    public void removeTracker(int rootId, String sessionId, int userId) {
        redis.opsForHash().delete(TRACKER + rootId, String.valueOf(userId + "//" + sessionId));
    }

    @Override
    public Map<Integer, Integer> getTrackerEntries(int rootId) {
        Map<Object, Object> entries = redis.opsForHash().entries(TRACKER + rootId);
        Map<Integer, Integer> result = new java.util.HashMap<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            try {
                int key = Integer.parseInt(entry.getKey().toString());
                int value = Integer.parseInt(entry.getValue().toString());
                result.put(key, value);
            } catch (NumberFormatException e) {
                // skip invalid entries
            }
        }
        return result;
    }

    @Override
    public void saveUserNickname(int userId, String nickname) {
        redis.opsForValue().set(NICKNAME + userId, nickname);
        redis.opsForValue().set(NAME_TO_ID + nickname, userId);
    }

    @Override
    public String getNicknameByUserId(int userId) {
        return (String) redis.opsForValue().get(NICKNAME + userId);
    }

    @Override
    public Integer getUserIdByNickname(String nickname) {
        Object v = redis.opsForValue().get(NAME_TO_ID + nickname);
        return v == null ? null : (Integer) v;
    }

    @Override
    public void mapUserToRoot(int rootId, int userId) {
        redis.opsForValue().set(USER_TO_ROOT + userId, rootId);
    }

    @Override
    public int getRootIdByUserId(int userId) {
        Object v = redis.opsForValue().get(USER_TO_ROOT + userId);
        return v == null ? 0 : (int) v;
    }

    @Override
    public int removeUserRootMapping(int userId) {
        Object v = redis.opsForValue().getAndDelete(USER_TO_ROOT + userId);
        return v == null ? 0 : (int) v;
    }
}

