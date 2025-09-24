package com.example.planmate.domain.shared.realtime.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RelationDelta {
    // For to-one: set target id (nullable clears the relation)
    private Object set;

    // For to-many: provide either set OR add/remove
    private List<Object> add;
    private List<Object> remove;
    private List<Object> setAll;

    public boolean isToOne() {
        return set != null && add == null && remove == null && setAll == null;
    }
}
