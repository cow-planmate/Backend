package com.example.planmate.domain.shared.sync.framework;

/**
 * A single step in a synchronization pipeline.
 * Implementations should be stateless and thread-safe.
 */
public interface SyncStep<C> {
    void execute(C ctx);
}
