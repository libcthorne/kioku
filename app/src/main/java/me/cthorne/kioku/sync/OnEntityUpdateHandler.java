package me.cthorne.kioku.sync;

/**
 * Created by chris on 19/01/16.
 */

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import cz.msebera.android.httpclient.Header;

/**
 * Handles individual entity updates.
 */
public class OnEntityUpdateHandler extends KiokuJsonHttpResponseHandler {
    private SyncInfo sync;
    private int entityTotalCount; // Number of entities that are expected to be updated
    private AtomicInteger entityCounter; // Number of entities updated so far
    private OnEntityUpdatesFinishedHandler finishedHandler;

    public OnEntityUpdateHandler(SyncInfo sync, int entityTotalCount, OnEntityUpdatesFinishedHandler finishedHandler) {
        this.sync = sync;
        this.entityTotalCount = entityTotalCount;
        this.entityCounter = new AtomicInteger();
        this.finishedHandler = finishedHandler;
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        // Ignore onSuccess calls if failed
        if (sync.getFailed().get())
            return;

        Log.d("kioku-sync", sync.getSyncItem().getEntityName() + " show success");
        Log.d("kioku-sync", sync.getSyncItem().getEntityName() + " show json: " + response);

        try {
            int syncId = response.getInt("i");

            SyncableItem entity = (SyncableItem)sync.getSyncItem().getDao().queryBuilder().where().eq("syncId", syncId).queryForFirst();

            // Create new entity if necessary
            if (entity == null) {
                entity = SyncableItem.newInstance(sync.getContext(), sync.getSyncItem().getEntityName());
                entity.setSyncId(syncId);
            }

            // Update word attributes
            entity.setSyncVersion(response.getInt("v"));
            entity.setSyncState(SyncableItem.SyncState.SYNCED);
            entity.onSync(sync.getDbHelper(), response);

            // Save
            sync.getSyncItem().getDao().createOrUpdate(entity);

            // Check if all updates finished
            if (entityCounter.incrementAndGet() == entityTotalCount) {
                Log.d("kioku-sync", "update " + entityCounter + "/" + entityTotalCount);
                finishedHandler.onSuccess();
            } else {
                sync.getMessenger().sendProgress("Fetched " + entityCounter + "/" + entityTotalCount + " " + sync.getSyncItem().getEntityNameForUser());

                Log.d("kioku-sync", "update " + entityCounter + "/" + entityTotalCount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("kioku-sync", "error processing " + sync.getSyncItem().getEntityName() + " show (sql)");
            finishedHandler.onFailure();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("kioku-sync", "error processing " + sync.getSyncItem().getEntityName() + " show (json)");
            finishedHandler.onFailure();
        }
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
        // Only fail once
        if (sync.getFailed().get())
            return;

        if (statusCode == 401)
            Log.e("kioku-sync", sync.getSyncItem().getEntityName() + " show: auth error! login first");

        Log.e("kioku-sync", sync.getSyncItem().getEntityName() + " show failure: " + responseString);

        finishedHandler.onFailure();
    }
}