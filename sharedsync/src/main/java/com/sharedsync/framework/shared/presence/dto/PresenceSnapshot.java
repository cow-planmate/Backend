package com.sharedsync.framework.shared.presence.dto;

import java.util.Map;

public record PresenceSnapshot(
        int userId,
        String nickname,
        Map<String, Object> attributes
) {}
