package com.sharedsync.shared.presence.storage;

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
    public boolean hasTracker(String rootId) {
        return redis.hasKey(TRACKER + rootId);
    }

    @Override
    public void insertTracker(String rootId, String sessionId, String userId, String index) {
        redis.opsForHash().put(TRACKER + rootId, String.valueOf(userId + "//" + sessionId), index);
    }

    @Override
    public void removeTracker(String rootId, String sessionId, String userId) {
        redis.opsForHash().delete(TRACKER + rootId, String.valueOf(userId + "//" + sessionId));
    }

    @Override
    public Map<String, String> getTrackerEntries(String rootId) {
        Map<Object, Object> entries = redis.opsForHash().entries(TRACKER + rootId);
        Map<String, String> result = new java.util.HashMap<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            try {
                String key = entry.getKey().toString();
                String value = entry.getValue().toString();
                result.put(key, value);
            } catch (NumberFormatException e) {
                // skip invalid entries
            }
        }
        return result;
    }

    @Override
    public void saveUserNickname(String userId, String nickname) {
        redis.opsForValue().set(NICKNAME + userId, nickname);
        redis.opsForValue().set(NAME_TO_ID + nickname, userId);
    }

    @Override
    public String getNicknameByUserId(String userId) {
        return (String) redis.opsForValue().get(NICKNAME + userId);
    }

    @Override
    public String getUserIdByNickname(String nickname) {
        Object v = redis.opsForValue().get(NAME_TO_ID + nickname);
        return v == null ? null : (String) v;
    }

    @Override
    public void mapUserToRoot(String rootId, String userId) {
        redis.opsForValue().set(USER_TO_ROOT + userId, rootId);
    }

    @Override
    public String getRootIdByUserId(String userId) {
        Object v = redis.opsForValue().get(USER_TO_ROOT + userId);
        return v == null ? null : (String) v;
    }

    @Override
    public String removeUserRootMapping(String userId) {
        Object v = redis.opsForValue().getAndDelete(USER_TO_ROOT + userId);
        return v == null ? null : (String) v;
    }
}

