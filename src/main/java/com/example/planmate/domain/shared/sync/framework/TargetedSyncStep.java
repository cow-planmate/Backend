package com.example.planmate.domain.shared.sync.framework;

/**
 * A SyncStep that targets a specific entity type. Used to auto-register
 * steps for relation-driven pipelines without hard-coded mappings.
 */
public interface TargetedSyncStep extends SyncStep<PlanSyncContext> {
    /**
     * The entity class this step is responsible for syncing.
     *
     * Note: By default this throws; CacheSyncService handles this by
     * inferring the target entity from the step class name (e.g.,
     * Sync{Entity}Step). Implementations may optionally override.
     */
    default Class<?> targetEntity() {
        throw new IllegalStateException("TargetedSyncStep " + this.getClass().getName() + " has no explicit targetEntity(). Name-based inference will be attempted by the registry builder.");
    }
}
