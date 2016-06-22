package me.cthorne.kioku.sync;

/**
 * Created by chris on 19/01/16.
 */

import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.msebera.android.httpclient.Header;

/**
 * Handles entity indexing (i.e. determining what entity data we need to request from the server).
 */
public class OnEntityIndexHandler extends KiokuJsonHttpResponseHandler {
    private SyncInfo sync;
    private OnEntityUpdatesFinishedHandler finishedHandler;

    public OnEntityIndexHandler(SyncInfo sync, OnEntityUpdatesFinishedHandler finishedHandler) {
        this.sync = sync;
        this.finishedHandler = finishedHandler;
    }

    /**
     * 1. Remove entities not in index response and in local.
     * 2. Create entities in index response and not in local.
     * 3. Fetch out-of-date entities.
     * @param response
     * @return
     */
    private Set<Integer> processResponse(JSONArray response) throws SQLException {
        // Get all local entities
        List<SyncableItem> localEntitiesList = sync.getSyncItem().getEntitySelector().query();

        // Index synced entities by sync_id for quick access using server IDs
        Map<Integer, SyncableItem> localEntitiesIndexed = new HashMap<>();
        for (SyncableItem localEntity : localEntitiesList) {
            if (localEntity.getSyncState() == SyncableItem.SyncState.NEW) {
                Log.d("kioku-sync", "ignoring local entity " + localEntity.id + " (not yet synced)");
                continue; // Ignore new entities which are not yet synced
            }

            Log.d("kioku-sync", "found synced local entity " + localEntity.id + "[s" + localEntity.getSyncId() + "," + localEntity.getSyncVersion() + "," + localEntity.getSyncState() + "]");

            localEntitiesIndexed.put(localEntity.getSyncId(), localEntity);
        }

        // Entity IDs that need fetching from server are added to this (for new creation or update)
        Set<Integer> entityIdsToFetch = new HashSet<>();

        try {
            // Go through and check server entities, comparing with local
            // Matching entities are removed from localEntitiesIndexed, so afterwards
            // localWordIndexed is only left with entities found on local but
            // not on server (and so need to be deleted)
            for (int i = 0; i < response.length(); i++) {
                JSONObject row = response.getJSONObject(i);
                int syncId = row.getInt("i");
                int syncVersion = row.getInt("v");
                Log.d("kioku-sync", "got syncId " + syncId + "(v" + syncVersion + ") from server");

                if (localEntitiesIndexed.containsKey(syncId)) {
                    Log.d("kioku-sync", "entity[s" + syncId + "] was in local");

                    // Check version and update if out-of-date
                    SyncableItem localEntity = localEntitiesIndexed.get(syncId);
                    if (syncVersion > localEntity.getSyncVersion()) {
                        if (localEntity.getSyncState() == SyncableItem.SyncState.SYNCED) {
                            Log.d("kioku-sync", "entity[s" + syncId + "] was in local but out-of-date");

                            // No changes since last sync and server is newer
                            entityIdsToFetch.add(syncId);
                        } else {
                            Log.d("kioku-sync", "entity[s" + syncId + "] was in local and out-of-date but has local changes that will be discarded");

                            // Conflict: server is newer but local has changed
                            // Solution: listen to server and discard local changes
                            // Possible future option: allow user to choose
                            entityIdsToFetch.add(syncId);
                        }
                    } else {
                        Log.d("kioku-sync", "entity[s" + syncId + "] is already synced");
                    }

                    // Matching entities are removed from localEntitiesIndexed
                    localEntitiesIndexed.remove(syncId);
                } else {
                    Log.d("kioku-sync", "entity[s" + syncId + "] was not in local");

                    // Entity not in local; need to fetch from server
                    entityIdsToFetch.add(syncId);
                }
            }

            // Check leftover words (words in local but not in server)
            for (SyncableItem localEntity : localEntitiesIndexed.values()) {
                Log.d("kioku-sync", "deleting local synced entity[s" + localEntity.getSyncId() + "] as it was not present on server");

                if (localEntity.getSyncState() != SyncableItem.SyncState.SYNCED) {
                    // Conflict: user has unsynced changes but entity has been deleted on the server
                    // Solution: listen to server and delete local
                    // Possible future option: allow user to choose
                }

                sync.getSyncItem().getDao().delete(localEntity);
            }

            return entityIdsToFetch;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void fetchEntity(Integer entityId, OnEntityUpdateHandler entityUpdateHandler) {
        // Get entity (entityName+'s/'+entityID, e.g. word 1 -> words/1)
        sync.getServerClient().getJS(sync.getSyncItem().getEntityName() + "s/" + entityId, null, entityUpdateHandler);
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
        // Ignore onSuccess calls if failed
        if (sync.getFailed().get())
            return;

        sync.getMessenger().sendProgress("Indexing " + sync.getSyncItem().getEntityNameForUser());

        Log.d("kioku-sync", sync.getSyncItem().getEntityName() + " index success");
        Log.d("kioku-sync", sync.getSyncItem().getEntityName() + " index json: " + response);

        try {
            // Work out entities that need syncing from index response
            Set<Integer> entityIdsToFetch = processResponse(response);
            if (entityIdsToFetch == null) {
                Log.e("kioku-sync", "process index response error");
                finishedHandler.onFailure();
                return;
            }

            Log.d("kioku-sync", entityIdsToFetch.size() + " entities to fetch");

            // Handles updates for each entity
            OnEntityUpdateHandler entityUpdateHandler = new OnEntityUpdateHandler(sync, entityIdsToFetch.size(), finishedHandler);

            if (entityIdsToFetch.size() == 0) {
                // Finish straightaway if there are no updates to fetch
                finishedHandler.onSuccess();
                return;
            }

            // Update each entity
            for (Integer id : entityIdsToFetch) {
                // Stop if failed
                if (sync.getFailed().get())
                    return;

                Log.d("kioku-sync", "need to fetch " + sync.getSyncItem().getEntityName() + " entity[s" + id + "]");
                fetchEntity(id, entityUpdateHandler);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("kioku-sync", "error processing " + sync.getSyncItem().getEntityName() + " index");
        }
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
        // Only fail once
        if (sync.getFailed().get())
            return;

        if (statusCode == 401) {
            Log.e("kioku-sync", sync.getSyncItem().getEntityName() + " index: auth error! force login first");
            Sync.authError = true;
        }

        Log.e("kioku-sync", sync.getSyncItem().getEntityName() + " index failure: " + responseString);

        finishedHandler.onFailure();
    }
}