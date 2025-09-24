package com.example.planmate.domain.shared.realtime.dto;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EntityPatch {
    private String entity;                // fully-qualified class name or JPA entity name
    private Object id;                    // PK value
    private Long expectedVersion;         // optional for optimistic lock
    private Map<String, Object> attributes; // scalar fields to update
    private Map<String, RelationDelta> relations; // relation deltas keyed by relation name
}
