package me.cthorne.kioku.sync;

import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cz.msebera.android.httpclient.Header;

/**
 * Created by chris on 20/01/16.
 */
public class OnMediaFileUpdatesFinishedHandler implements OnEntitySyncStageFinishedHandler {
    private SyncInfo sync;
    private OnEntitySyncStageFinishedHandler finishedHandler;

    public OnMediaFileUpdatesFinishedHandler(SyncInfo sync, OnEntitySyncStageFinishedHandler finishedHandler) {
        this.sync = sync;
        this.finishedHandler = finishedHandler;
    }

    /**
     * Push local changes and newly created entities to the server.
     */
    private void pushLocalEntities() throws SQLException {
        Log.d("kioku-sync", "pushing local " + sync.getSyncItem().getEntityName() + " entities");

        List<SyncableItem> entityList = sync.getSyncItem().getEntitySelector().query();

        final int entityTotalCount = entityList.size(); // Number of entities that are expected to be looked at
        final AtomicInteger entityCounter = new AtomicInteger(); // Number of entities processed so far

        if (entityTotalCount == 0)
            finishedHandler.onSuccess();

        for (SyncableItem entity : entityList) {
            // Stop on failure
            if (sync.getFailed().get())
                return;

            if (entity.getSyncState() == SyncableItem.SyncState.SYNCED) {
                Log.d("kioku-sync", "push " + sync.getSyncItem().getEntityName() + ": entity[s" + entity.getSyncId() + "] doesn't need pushing");

                if (entityCounter.incrementAndGet() == entityTotalCount)
                    finishedHandler.onSuccess();
                else
                    sync.getMessenger().sendProgress("Pushed " + entityCounter.get() + "/" + entityTotalCount +
                            " " + sync.getSyncItem().getEntityNameForUser());

                continue;
            }

            if (entity.getSyncState() == SyncableItem.SyncState.NEW) {
                Log.d("kioku-sync", "push " + sync.getSyncItem().getEntityName() + ": new entity (local id: " + entity.id + ")");

                RequestParams params = new RequestParams();
                params.add("local_id", String.valueOf(entity.id));
                entity.fillSyncParams(params);

                sync.getServerClient().postJS(sync.getSyncItem().getEntityName() + "s/", params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        // Stop on failure
                        if (sync.getFailed().get())
                            return;

                        Log.d("kioku-sync", "added entity: " + response);

                        try {
                            SyncableItem entity = (SyncableItem) sync.getSyncItem().getDao().queryForId(response.getInt("local_id"));
                            entity.setSyncState(SyncableItem.SyncState.SYNCED);
                            entity.setSyncId(response.getInt("i"));
                            entity.setSyncVersion(response.getInt("v"));
                            entity.onSync(sync.getDbHelper(), response);
                            sync.getSyncItem().getDao().update(entity);

                            if (entityCounter.incrementAndGet() == entityTotalCount)
                                finishedHandler.onSuccess();
                            else
                                sync.getMessenger().sendProgress("Pushed " + entityCounter.get() + "/" + entityTotalCount +
                                        " " + sync.getSyncItem().getEntityNameForUser());
                        } catch (SQLException | JSONException e) {
                            e.printStackTrace();
                            Log.e("kioku-sync", "push " + sync.getSyncItem().getEntityName() + ": error processing response");
                            finishedHandler.onFailure();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        // Stop on failure
                        if (sync.getFailed().get())
                            return;

                        Log.e("kioku-sync", "error adding entity: " + responseString);
                        finishedHandler.onFailure();
                    }
                });
            } else if (entity.getSyncState() == SyncableItem.SyncState.UPDATED) {
                Log.d("kioku-sync", "push " + sync.getSyncItem().getEntityName() + ": updated entity[s" + entity.getSyncId() + "]");

                RequestParams params = new RequestParams();
                params.add("local_id", String.valueOf(entity.id));
                params.add("v", String.valueOf(entity.getSyncVersion()));
                params.add("_method", "put"); // for Rails updating
                entity.fillSyncParams(params);

                sync.getServerClient().postJS(sync.getSyncItem().getEntityName() + "s/" + entity.getSyncId(), params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        // Stop on failure
                        if (sync.getFailed().get())
                            return;

                        Log.d("kioku-sync", "updated entity: " + response);

                        try {
                            SyncableItem entity = (SyncableItem) sync.getSyncItem().getDao().queryForId(response.getInt("local_id"));
                            entity.setSyncState(SyncableItem.SyncState.SYNCED);
                            sync.getSyncItem().getDao().update(entity);

                            if (entityCounter.incrementAndGet() == entityTotalCount)
                                finishedHandler.onSuccess();
                            else
                                sync.getMessenger().sendProgress("Pushed " + entityCounter.get() + "/" + entityTotalCount +
                                        " " + sync.getSyncItem().getEntityNameForUser());
                        } catch (SQLException | JSONException e) {
                            e.printStackTrace();
                            Log.e("kioku-sync", "push(update) " + sync.getSyncItem().getEntityName() + ": error processing response");
                            finishedHandler.onFailure();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        // Stop on failure
                        if (sync.getFailed().get())
                            return;

                        Log.e("kioku-sync", "error updating entity: " + responseString);
                        finishedHandler.onFailure();
                    }
                });
            } else {
                Log.d("kioku-sync", "push " + sync.getSyncItem().getEntityName() + ": deleted entity[s" + entity.getSyncId() + "]");

                RequestParams params = new RequestParams();
                params.add("local_id", String.valueOf(entity.id));
                params.add("_method", "delete"); // for Rails deletion

                sync.getServerClient().postJS(sync.getSyncItem().getEntityName() + "s/" + entity.getSyncId(), params, new KiokuJsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        // Ignore onSuccess calls if failed
                        if (sync.getFailed().get())
                            return;

                        Log.d("kioku-sync", "deleted entity: " + response);

                        try {
                            // Find and delete local deleted marker for entity
                            SyncableItem entity = (SyncableItem) sync.getSyncItem().getDao().queryForId(response.getInt("local_id"));
                            sync.getSyncItem().getDao().delete(entity);

                            if (entityCounter.incrementAndGet() == entityTotalCount)
                                finishedHandler.onSuccess();
                            else
                                sync.getMessenger().sendProgress("Pushed " + entityCounter.get() + "/" + entityTotalCount +
                                                                    " " + sync.getSyncItem().getEntityNameForUser());
                        } catch (SQLException | JSONException e) {
                            e.printStackTrace();
                            Log.e("kioku-sync", "push (del) " + sync.getSyncItem().getEntityName() + ": error processing response");
                            finishedHandler.onFailure();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        // Only fail once
                        if (sync.getFailed().get())
                            return;

                        Log.e("kioku-sync", "error deleting entity: " + responseString);
                        finishedHandler.onFailure();
                    }
                });
            }
        }
    }


    @Override
    public void onSuccess() {
        // Ignore onSuccess calls if failed
        if (sync.getFailed().get())
            return;

        Log.d("kioku-sync", "finished getting media files");
        try {
            pushLocalEntities();
        } catch (SQLException e) {
            e.printStackTrace();;
            Log.e("kioku-sync", "sql error pushing local entities");
        }
    }

    @Override
    public void onFailure() {
        // Only fail once
        if (sync.getFailed().get())
            return;

        Log.e("kioku-sync", "error getting media files");

        finishedHandler.onFailure();
    }
}