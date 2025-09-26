package com.example.planmate.domain.shared.sync.framework;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple pipeline that runs registered steps sequentially.
 */
public class SyncPipeline<C> {
    private final List<SyncStep<C>> steps = new ArrayList<>();

    public SyncPipeline<C> add(SyncStep<C> step) {
        steps.add(step);
        return this;
    }

    public void run(C ctx) {
        for (SyncStep<C> step : steps) {
            step.execute(ctx);
        }
    }
}
