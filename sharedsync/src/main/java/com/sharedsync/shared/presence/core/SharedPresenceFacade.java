package com.sharedsync.shared.presence.core;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.sharedsync.shared.presence.dto.PresenceSnapshot;
import com.sharedsync.shared.presence.storage.PresenceStorage;

import java.util.*;

@Component
@RequiredArgsConstructor
public class SharedPresenceFacade {

    private final PresenceStorage storage;

    public List<PresenceSnapshot> getPresence(int planId) {
        var entries = storage.getTrackerEntries(planId);

        if (entries == null || entries.isEmpty()) return Collections.emptyList();

        List<PresenceSnapshot> snapshots = new ArrayList<>();

        for (var e : entries.entrySet()) {
            int userId = e.getKey();
            int dayIndex = e.getValue();
            String nickname = storage.getNicknameByUserId(userId);

            Map<String, Object> attr = new HashMap<>();
            attr.put("dayIndex", dayIndex);

            snapshots.add(new PresenceSnapshot(
                    userId,
                    nickname,
                    attr
            ));
        }
        return snapshots;
    }
}
