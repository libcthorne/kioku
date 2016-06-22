package me.cthorne.kioku.infosources;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.KiokuServerClient;
import me.cthorne.kioku.MainActivity;
import me.cthorne.kioku.words.WordLanguage;

/**
 * Created by chris on 02/03/16.
 */
public class SourcesUpdater {

    private static int updatesProgress;
    private static int updatesTotal;

    private static final int UPDATE_CONNECT_ERROR = 0;
    private static final int UPDATE_INDEX_ERROR = 1;
    private static final int UPDATE_GOT_INDEX = 2;
    private static final int UPDATE_FINISHED_NO_CHANGES = 3;
    private static final int UPDATE_INDEX_PROCESS_ERROR = 4;
    private static final int UPDATE_DOWNLOADING = 5;
    private static final int UPDATE_FINISHED = 6;
    private static final int UPDATE_DOWNLOAD_ERROR = 7;

    public static ProgressDialog updateDialog;
    private static Handler updateHandler;

    private Activity activity;
    private DatabaseHelper dbHelper;
    private WordLanguage language;
    private SourcesUpdaterHandler handler;

    private KiokuServerClient serverClient;

    public interface SourcesUpdaterHandler {
        void onSuccess(String message);
        void onError(String message);
    }

    public SourcesUpdater(Activity activity, DatabaseHelper dbHelper, WordLanguage language) {
        this.activity = activity;
        this.dbHelper = dbHelper;
        this.language = language;
        this.serverClient = new KiokuServerClient(activity);
    }

    public void start(SourcesUpdaterHandler handler, boolean showDialog) {
        this.handler = handler;

        updateDialog = new ProgressDialog(activity);
        updateDialog.setTitle("Checking for updates");
        updateDialog.setMessage("Connecting to server");
        updateDialog.setCancelable(false);
        updateDialog.setIndeterminate(true);
        /*updateDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d("kioku-sources", "update dialog dismissed");
                loadSources();
            }
        });*/

        if (showDialog) {
            // Show popup loading dialog
            updateDialog.show();
        }

        // Sets dialog progress in UI thread
        updateHandler = new UpdateHandler(activity, handler);

        requestWISHIndex();
    }

    public void start(SourcesUpdaterHandler handler) {
        start(handler, true);
    }

    /**
     * Request latest sources information from server.
     */
    private void requestWISHIndex() {
        RequestParams params = new RequestParams();
        params.add("language", String.valueOf(MainActivity.currentLanguage.getValue()));
        serverClient.getJS("word_information_sources/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("kioku-sources", "got index: " + response);
                updateHandler.obtainMessage(UPDATE_GOT_INDEX).sendToTarget();
                processWISHIndex(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("kioku-sources", "error getting index: " + responseString);
                updateHandler.obtainMessage(UPDATE_INDEX_ERROR).sendToTarget();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.d("kioku-sources", "js error getting index: " + response);
                updateHandler.obtainMessage(UPDATE_INDEX_ERROR).sendToTarget();
            }
        });
    }

    private void processWISHIndex(JSONArray response) {
        try {
            // Get user's selected sources
            List<SelectedWordInformationSource> userSources = dbHelper.qbSelectedUserSources(false, language)
                    .leftJoin(dbHelper.getWordInformationSourceDao().queryBuilder())
                    .query();

            // Map source names with versions for quick version comparison
            Map<String, Integer> sourceVersions = new HashMap<>();
            for (SelectedWordInformationSource source : userSources) {
                source.loadSource(dbHelper);
                // Note: version will be 0 for non-downloaded sources so they will always be downloaded
                sourceVersions.put(source.getSource().getName(), source.getSource().getVersion());
            }

            // Find sources that need updating
            List<String> sourcesToUpdate = new ArrayList<>();
            for (int i = 0; i < response.length(); i++) {
                JSONObject sourceJSON = response.getJSONObject(i);

                Log.d("kioku-sources", "source: " + sourceJSON);

                String name = sourceJSON.getString("name");
                int version = sourceJSON.getInt("version");

                if (!sourceVersions.containsKey(name)) {
                    Log.d("kioku-sources", "source " + name + " not used");
                } else if (sourceVersions.get(name) < version) {
                    sourcesToUpdate.add(name);
                    Log.d("kioku-sources", "source " + name + " needs updating (l" + sourceVersions.get(name) + "/s" + version + ")");
                } else {
                    Log.d("kioku-sources", "source " + name + " is up-to-date (l" + sourceVersions.get(name) + "/s" + version + ")");
                }
            }

            // Start updates
            downloadWISHUpdates(sourcesToUpdate);
        } catch (JSONException | SQLException e) {
            e.printStackTrace();
            updateHandler.obtainMessage(UPDATE_INDEX_PROCESS_ERROR).sendToTarget();
        }
    }

    private void downloadWISHUpdates(List<String> sourcesToUpdate) {
        if (sourcesToUpdate.size() == 0) {;
            updateHandler.obtainMessage(UPDATE_FINISHED_NO_CHANGES).sendToTarget();
            return;
        }

        updateHandler.obtainMessage(UPDATE_DOWNLOADING).sendToTarget();

        SourcesDownloader downloader = new SourcesDownloader(activity, dbHelper, sourcesToUpdate, language, true);
        downloader.start(new SourcesDownloader.OnInfoSourceDownloadsFinishedHandler() {
            @Override
            public void onSuccess() {
                updateHandler.obtainMessage(UPDATE_FINISHED).sendToTarget();
            }

            @Override
            public void onFailure() {
                updateHandler.obtainMessage(UPDATE_DOWNLOAD_ERROR).sendToTarget();
            }

            @Override
            public void onProgress(int done, int total) {
                updatesProgress = done;
                updatesTotal = total;
                updateHandler.obtainMessage(UPDATE_DOWNLOADING).sendToTarget();
            }
        });
    }

    private static class UpdateHandler extends Handler {
        private Activity activity;
        private SourcesUpdaterHandler handler;

        public UpdateHandler(Activity activity, SourcesUpdaterHandler handler) {
            this.activity = activity;
            this.handler = handler;
        }

        public void finishUpdate(boolean success, String message) {
            updateDialog.dismiss();

            if (success) {
                SharedPreferences preferences = KiokuServerClient.getPreferences(activity);
                preferences.edit().putLong("lastSourcesUpdate", new DateTime().getMillis()).commit();

                handler.onSuccess(message);
            } else {
                handler.onError(message);
            }
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // Progress
                case UPDATE_GOT_INDEX:
                    updateDialog.setMessage("Comparing versions");
                    break;
                case UPDATE_FINISHED_NO_CHANGES:
                    finishUpdate(true, "No updates found.");
                    break;
                case UPDATE_DOWNLOADING:
                    updateDialog.setMessage("Updating source " + updatesProgress + "/" + updatesTotal);
                    break;
                case UPDATE_FINISHED:
                    finishUpdate(true, "Updates finished successfully");
                    break;

                // Errors
                case UPDATE_CONNECT_ERROR:
                    finishUpdate(false, "Error connecting to word sources server. Please try again later.");
                    break;
                case UPDATE_INDEX_ERROR:
                    finishUpdate(false, "Error retrieving word sources. Please try again later.");
                    break;
                case UPDATE_INDEX_PROCESS_ERROR:
                    finishUpdate(false, "Error processing server index response.");
                    break;
                case UPDATE_DOWNLOAD_ERROR:
                    finishUpdate(false, "Error downloading updates.");
                    break;
            }
        }
    }

}
