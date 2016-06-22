package me.cthorne.kioku.sync;

import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import cz.msebera.android.httpclient.Header;
import me.cthorne.kioku.Utils;

/**
 * Callbacks for after all entities have been pushed.
 */
public class OnEntityPushesFinishedHandler implements OnEntitySyncStageFinishedHandler {
    private SyncInfo sync;
    private OnEntitySyncStageFinishedHandler finishedHandler;

    public OnEntityPushesFinishedHandler(SyncInfo sync, OnEntitySyncStageFinishedHandler finishedHandler) {
        this.sync = sync;
        this.finishedHandler = finishedHandler;
    }

    /**
     * Upload media files for WordInformations.
     * @throws SQLException
     */
    private void pushMediaFiles() throws SQLException {
        sync.getServerClient().getJS("media_files/?mode=upload", null, new KiokuJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("kioku-sync", "media files server wants:" + response);

                final int totalFiles = response.length();

                if (totalFiles == 0)
                    finishedHandler.onSuccess();

                final AtomicInteger uploadedCounter = new AtomicInteger();

                try {
                    for (int i = 0; i < totalFiles; i++) {
                        JSONObject fileJson = response.getJSONObject(i);

                        final String fileName = fileJson.getString("name");
                        File file = Utils.mediaFile(sync.getContext(),fileName);

                        Log.d("kioku-sync", "server wants " + fileName + "(exists: " + file.exists() + ")");

                        try {
                            RequestParams params = new RequestParams();
                            params.put("file", file);

                            sync.getServerClient().post("media_files/", params, new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    Log.d("kioku-sync", "uploaded " + fileName);

                                    if (uploadedCounter.incrementAndGet() == totalFiles)
                                        finishedHandler.onSuccess();
                                    else
                                        sync.getMessenger().sendProgress("Uploaded " + uploadedCounter.get() + "/" + totalFiles + " media files");
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                    Log.e("kioku-sync", "error uploading " + fileName + ": " + new String(responseBody));
                                    finishedHandler.onFailure();
                                }
                            });
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Log.e("kioku-sync", "server requested file client doesn't have: " + fileName);
                            finishedHandler.onFailure();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    finishedHandler.onFailure();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                finishedHandler.onFailure();
            }
        });
    }

    @Override
    public void onSuccess() {
        // Ignore onSuccess calls if failed
        if (sync.getFailed().get())
            return;

        Log.d("kioku-sync", "finished pushing");

        if (sync.getSyncItem().getEntityName() != "word_information") {
            finishedHandler.onSuccess();
            return;
        }

        // Push word information media files
        try {
            pushMediaFiles();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("kioku-sync", "error pushing media files");
        }
    }

    @Override
    public void onFailure() {
        // Only fail once
        if (sync.getFailed().get())
            return;

        Log.e("kioku-sync", "error pushing");

        finishedHandler.onFailure();
    }
}