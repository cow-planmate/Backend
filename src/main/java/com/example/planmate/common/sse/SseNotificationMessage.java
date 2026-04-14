package com.example.planmate.common.sse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload published to the Redis SSE channel.
 * Serialized as JSON so any node can deserialize and deliver to its local emitters.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SseNotificationMessage {
    /** Target user's UUID as a string. */
    private String userId;
    /** SSE event name (e.g. "invitation", "requestResult"). */
    private String eventName;
    /** Arbitrary event data – serialized/deserialized by Jackson. */
    private Object data;
}
