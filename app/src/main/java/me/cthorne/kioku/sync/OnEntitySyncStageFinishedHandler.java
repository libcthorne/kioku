package me.cthorne.kioku.sync;

/**
 * Callbacks for when a sync stage has finished.
 */
public interface OnEntitySyncStageFinishedHandler {
    void onSuccess();
    void onFailure();
}
