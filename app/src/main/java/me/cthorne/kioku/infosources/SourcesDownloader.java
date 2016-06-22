package me.cthorne.kioku.infosources;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import cz.msebera.android.httpclient.Header;
import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.KiokuServerClient;
import me.cthorne.kioku.words.WordLanguage;

/**
 * Created by chris on 24/01/16.
 */
public class SourcesDownloader {

    public interface OnInfoSourceDownloadsFinishedHandler {
        void onSuccess();
        void onFailure();
        void onProgress(int done, int total);
    }

    private Context context;
    private DatabaseHelper dbHelper;
    private KiokuServerClient serverClient;
    private List<String> sourceNames;
    private WordLanguage language;
    private boolean updateMode;

    public SourcesDownloader(Context context, DatabaseHelper dbHelper, List<String> sourceNames, WordLanguage language, boolean updateMode) {
        this.context = context;
        this.dbHelper = dbHelper;
        this.serverClient = new KiokuServerClient(context);
        this.sourceNames = sourceNames;
        this.language = language;
        this.updateMode = updateMode;
    }

    public void start(final OnInfoSourceDownloadsFinishedHandler handler) {
        final AtomicInteger downloadedCount = new AtomicInteger();

        // Finish straightaway if there is nothing to download
        if (sourceNames.size() == 0) {
            handler.onSuccess();
            return;
        }

        // Convert list of requested sources into string param, each source separated by '?'
        String namesStr = "";
        for (String sourceName : sourceNames) {
            if (!namesStr.isEmpty())
                namesStr += "?"; // separator

            namesStr += sourceName;
        }

        RequestParams params = new RequestParams();
        params.add("s", namesStr);

        serverClient.getJS("word_information_sources/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("kioku-sources", "got sources: " + response);

                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject sourceJson = response.getJSONObject(i);

                        // Read JSON fields
                        String name = sourceJson.getString("name");
                        int version = sourceJson.getInt("version");
                        String title = sourceJson.getString("title");
                        String url = sourceJson.getString("url");
                        String selectJS = sourceJson.optString("select_js");
                        String saveJS = sourceJson.optString("save_js");

                        // Create object
                        final WordInformationSource source = new WordInformationSource(name, version, title, url, selectJS, saveJS);
                        // Get DAOs
                        final Dao<WordInformationSource, Integer> sourceDao = dbHelper.getWordInformationSourceDao();
                        final Dao<SelectedWordInformationSource, Integer> selectedSourceDao = dbHelper.getSelectedWordInformationSourceDao();

                        TransactionManager.callInTransaction(dbHelper.getConnectionSource(),
                                new Callable<Void>() {
                                    public Void call() throws Exception {
                                        // Try and save source in DB (if it doesn't exist already)
                                        sourceDao.createOrUpdate(source);

                                        if (!updateMode) {
                                            // Get last position
                                            long maxPosition = selectedSourceDao.queryRawValue("SELECT MAX(position) FROM selected_word_information_sources WHERE language = ?", Integer.toString(language.getValue()));
                                            SelectedWordInformationSource lastSelectedSource = selectedSourceDao.queryBuilder().where().eq("language", language.getValue()).and().eq("position", maxPosition).queryForFirst();
                                            int lastPosition = lastSelectedSource == null ? 0 : lastSelectedSource.getPosition() + 1;
                                            Log.d("kioku-sources", "lastPosition: " + lastPosition);

                                            // Save source as selected
                                            SelectedWordInformationSource selectedSource = new SelectedWordInformationSource(context, source, language, lastPosition);
                                            selectedSourceDao.createOrUpdate(selectedSource);
                                        }

                                        // Check if all downloads are finished
                                        if (downloadedCount.incrementAndGet() == sourceNames.size()) {
                                            handler.onSuccess();
                                        } else {
                                            handler.onProgress(downloadedCount.get(), sourceNames.size());
                                            Log.d("kioku-sources", "progress: " + downloadedCount + "/" + sourceNames.size());
                                        }

                                        return null;
                                    }
                                });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("kioku-sources", "error processing downloaded word source");
                    handler.onFailure();
                } catch (SQLException e) {
                    e.printStackTrace();
                    Log.d("kioku-sources", "error saving downloaded word source to db");
                    handler.onFailure();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("kioku-sources", "error getting source: " + responseString);
                handler.onFailure();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.d("kioku-sources", "error connecting to server: " + response);
                handler.onFailure();
            }

        });
    }



}
