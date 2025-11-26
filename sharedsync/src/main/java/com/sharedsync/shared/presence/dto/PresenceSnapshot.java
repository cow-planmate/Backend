package com.sharedsync.shared.presence.dto;

import java.util.Map;

public record PresenceSnapshot(
        String userId,
        String nickname,
        Map<String, Object> attributes
) {}
