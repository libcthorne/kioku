package me.cthorne.kioku.sync;

import android.util.Log;

import com.loopj.android.http.FileAsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import cz.msebera.android.httpclient.Header;
import me.cthorne.kioku.Utils;

public class OnEntityUpdatesFinishedHandler implements OnEntitySyncStageFinishedHandler {
    private SyncInfo sync;
    private OnEntitySyncStageFinishedHandler finishedHandler;

    public OnEntityUpdatesFinishedHandler(SyncInfo sync, OnEntitySyncStageFinishedHandler finishedHandler) {
        this.sync = sync;
        this.finishedHandler = finishedHandler;
    }

    @Override
    public void onSuccess() {
        // Ignore onSuccess calls if failed
        if (sync.getFailed().get())
            return;

        Log.d("kioku-sync", "finished updating");

        if (sync.getSyncItem().getEntityName() != "word_information") {
            finishedHandler.onSuccess();
            return;
        }

        // Get media files for WordInformations
        sync.getServerClient().getJS("media_files/", null, new KiokuJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("kioku-sync", "media files we need:" + response);

                final int totalFiles = response.length();

                if (totalFiles == 0)
                    finishedHandler.onSuccess();

                final AtomicInteger downloadedCounter = new AtomicInteger();

                try {
                    for (int i = 0; i < totalFiles; i++) {
                        JSONObject fileJson = response.getJSONObject(i);

                        final String fileName = fileJson.getString("name");
                        final File mediaFile = Utils.mediaFile(sync.getContext(), fileName);

                        Log.d("kioku-sync", "we need " + fileName + "(exists: " + mediaFile.exists() + "," + mediaFile.getAbsolutePath() + ")");

                        if (mediaFile.exists()) {
                            if (downloadedCounter.incrementAndGet() == totalFiles)
                                finishedHandler.onSuccess();
                            else
                                Log.d("kioku-sync", "media file " + downloadedCounter.get() + "/" + totalFiles + " (already existed)");

                            continue;
                        }

                        // Download media file
                        sync.getServerClient().get("uploads/media/" + fileName, null, new FileAsyncHttpResponseHandler(sync.getContext()) {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, File response) {
                                // Ignore onSuccess calls if failed
                                if (sync.getFailed().get())
                                    return;

                                Log.d("kioku-sync", "got file " + fileName);

                                if (file.renameTo(mediaFile)) {
                                    if (downloadedCounter.incrementAndGet() == totalFiles)
                                        finishedHandler.onSuccess();
                                    else
                                        sync.getMessenger().sendProgress("Downloaded " + downloadedCounter.get() + "/" + totalFiles + " media files");
                                } else {
                                    Log.d("kioku-sync", "error saving file " + fileName);
                                    finishedHandler.onFailure();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                                Log.d("kioku-sync", "error downloading file " + fileName);
                                finishedHandler.onFailure();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("kioku-sync", "json error downloading media files");
                    finishedHandler.onFailure();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                // Only fail once
                if (sync.getFailed().get())
                    return;

                finishedHandler.onFailure();
            }
        });
    }

    @Override
    public void onFailure() {
        // Only fail once
        if (sync.getFailed().get())
            return;

        Log.e("kioku-sync", "error updating");

        finishedHandler.onFailure();
    }
}