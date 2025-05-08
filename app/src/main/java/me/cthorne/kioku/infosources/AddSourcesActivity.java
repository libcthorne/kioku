package me.cthorne.kioku.infosources;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.KiokuServerClient;
import me.cthorne.kioku.MainActivity;
import me.cthorne.kioku.R;
import me.cthorne.kioku.words.WordLanguage;
import me.cthorne.kioku.orm.OrmLiteBaseActivityCompat;

/**
 * Created by chris on 23/01/16.
 */
public class AddSourcesActivity extends OrmLiteBaseActivityCompat<DatabaseHelper> {

    private ArrayList<SelectableWordInformationSource> sources;
    private ListView sourcesListView;
    private AddSourcesListViewAdapter mArrayAdapter;
    private KiokuServerClient serverClient;
    private RequestHandle indexRequestHandle;
    private WordLanguage language;

    private RelativeLayout firstHint;

    private static RelativeLayout loadingOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sources);

        firstHint = (RelativeLayout)findViewById(R.id.first_hint);

        if (MainActivity.isInTutorial())
            firstHint.setVisibility(View.VISIBLE);

        // Get language param
        language = WordLanguage.fromInt(getIntent().getExtras().getInt("language"));

        final Context context = this;

        // List of all available sources
        sources = new ArrayList<>();

        // Adapter for user words list view
        mArrayAdapter = new AddSourcesListViewAdapter(this, sources);

        // List view
        sourcesListView = (ListView) findViewById(R.id.sources_listview);
        sourcesListView.setAdapter(mArrayAdapter);

        // Loading overlay
        loadingOverlay = (RelativeLayout) findViewById(R.id.loading_overlay);
        loadingOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        // Hides loading overlay in UI thread
        final Handler handler = new LoadingHandler();

        // Init client
        serverClient = new KiokuServerClient(this);

        // Request word sources
        RequestParams params = new RequestParams();
        params.add("language", String.valueOf(language.getValue()));
        indexRequestHandle = serverClient.getJS("word_information_sources/", params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("kioku-sources", "got index: " + response);

                // Process sources
                processSources(response);

                // Hide loading overlay
                handler.obtainMessage(1).sendToTarget();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("kioku-sources", "error getting index: " + responseString);

                // Show error
                Toast.makeText(context, "Error retrieving word sources. Please try again later.", Toast.LENGTH_SHORT).show();
                // Go back
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.d("kioku-sources", "error connecting to server: " + response);

                // Show error
                Toast.makeText(context, "Error connecting to word sources server. Please try again later.", Toast.LENGTH_SHORT).show();
                // Go back
                finish();
            }

        });

        // Display up button (required as no ParentActivity is specified)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Cancel index request if active
        if (indexRequestHandle != null)
            indexRequestHandle.cancel(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_sources, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_sources_button) {
            addSelectedSources();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void processSources(JSONArray response) {
        // Check if any sources were found
        if (response.length() == 0) {
            Toast.makeText(this, "No information sources were found for this language.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            // Get current source IDs

            List<SelectedWordInformationSource> currentSources = getHelper().qbSelectedUserSources(false, language).query();
            ArrayList<String> currentSourceNames = new ArrayList<>();
            for (SelectedWordInformationSource source : currentSources)
                currentSourceNames.add(source.getSourceName());

            // Add sources found in response to list view

            for (int i = 0; i < response.length(); i++) {
                JSONObject sourceJSON = response.getJSONObject(i);

                Log.d("kioku-sources", "adding source: " + sourceJSON);

                String name = sourceJSON.getString("name");
                String title = sourceJSON.getString("title");
                String url = sourceJSON.getString("url");
                boolean recommended = sourceJSON.getBoolean("recommended");
                boolean disabled = currentSourceNames.contains(name); // disable sources the user already has

                sources.add(new SelectableWordInformationSource(name, title, url, recommended, disabled));
            }

            // Refresh ListView data
            mArrayAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading information sources. Please try again later.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading existing information sources.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void addSelectedSources() {
        // Show loading overlay
        loadingOverlay.setVisibility(View.VISIBLE);

        // Get selected sources
        ArrayList<String> selectedSourceNames = new ArrayList<>();
        for (SelectableWordInformationSource source : sources) {
            if (!source.isSelected() || source.isDisabled())
                continue;

            selectedSourceNames.add(source.getSource().getName());
        }

        final Context context = this;

        // Download selected sources
        SourcesDownloader downloader = new SourcesDownloader(this, getHelper(), selectedSourceNames, language, false);
        downloader.start(new SourcesDownloader.OnInfoSourceDownloadsFinishedHandler() {

            @Override
            public void onSuccess() {
                Log.d("kioku-sources", "finished downloading sources");
                finish();
            }

            @Override
            public void onFailure() {
                Log.d("kioku-sources", "error downloading sources");
                Toast.makeText(context, "Error downloading sources. Please try again later.", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onProgress(int done, int total) {
            }

        });
    }

    private static class LoadingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            loadingOverlay.setVisibility(View.GONE);
        }
    }
}
