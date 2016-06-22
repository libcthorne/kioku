package me.cthorne.kioku.infosources;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivityCompat;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.mobeta.android.dslv.DragSortListView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.R;
import me.cthorne.kioku.words.WordLanguage;

/**
 * Created by chris on 23/01/16.
 */
public class SourcesActivity extends OrmLiteBaseActivityCompat<DatabaseHelper> implements AdapterView.OnItemClickListener {

    private static final int ADD_SOURCE_FOR_SEARCH_RESULT_ID = 10;

    private ArrayList<SelectedWordInformationSource> sources = new ArrayList<>();
    private DragSortListView sourcesListView;
    private SourcesListViewAdapter mArrayAdapter;
    private boolean editMode;
    private WordLanguage language;
    private boolean noSources;

    private boolean doneAddingSources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("kioku-search", "oncreate sourcesactivity");

        setContentView(R.layout.activity_sources);

        // Display up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get language param
        language = WordLanguage.fromInt(getIntent().getExtras().getInt("language"));

        // Adapter for user words list view
        mArrayAdapter = new SourcesListViewAdapter(this, sources);

        // List view
        sourcesListView = (DragSortListView)findViewById(R.id.sources_listview);
        sourcesListView.setDragEnabled(false); // drag enabled only in edit mode
        sourcesListView.setAdapter(mArrayAdapter);
        sourcesListView.setOnItemClickListener(this);
        sourcesListView.setDropListener(new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                SelectedWordInformationSource fromObj = sources.get(from);

                sources.remove(from);
                sources.add(to, fromObj);

                mArrayAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("kioku-search", "onActivityResult sourcesactivity " + requestCode + "/" + resultCode);

        // User was adding sources for search; go back to search now
        if (requestCode == ADD_SOURCE_FOR_SEARCH_RESULT_ID) {
            finish();
            doneAddingSources = true;
            Log.d("kioku-search", "onActivityResult sourcesactivity set to leave");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (doneAddingSources)
            return;

        Log.d("kioku-search", "onresume sourcesactivity [done;" + doneAddingSources + ", fin:" + isFinishing() + "]");

        // Show user's word sources
        // This is called in resume as these can change during the activity's lifetime (by user deletion or addition)
        loadSources();

        // Go straight to adding sources if this was a redirect from search and the user has no sources
        if (getIntent().getExtras().getBoolean("fromSearch", false)) {
            if (sources.size() == 0) {
                Intent addSourceIntent = new Intent(this, AddSourcesActivity.class);
                addSourceIntent.putExtra("language", language.getValue());
                addSourceIntent.putExtra("fromSearch", true);
                startActivityForResult(addSourceIntent, ADD_SOURCE_FOR_SEARCH_RESULT_ID);
            }
        }
    }

    private void loadSources() {
        sources.clear();

        try {
            List<SelectedWordInformationSource> selectedSources = getHelper().qbSelectedUserSources(false, language).query();

            Log.d("kioku-sources", "user has " + selectedSources.size() + " sources");

            for (SelectedWordInformationSource selectedSource : selectedSources) {
                Log.d("kioku-sources", "source at " + selectedSource.getPosition() + ": " + selectedSource.getSource().getName());

                selectedSource.loadSource(getHelper());
                sources.add(selectedSource);
            }

            mArrayAdapter.notifyDataSetChanged();
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error getting current sources.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Determine if there are any sources
        noSources = sources.isEmpty();
        // Redraw menu as it depends on the value of noSources
        invalidateOptionsMenu();

        // Show "add sources" text hint if there are no sources
        TextView addSourcesHint = (TextView)findViewById(R.id.add_sources_hint);
        addSourcesHint.setVisibility(noSources ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (noSources) {
            getMenuInflater().inflate(R.menu.menu_sources_empty, menu);
        } else {
            if (editMode)
                getMenuInflater().inflate(R.menu.menu_sources_edit, menu);
            else
                getMenuInflater().inflate(R.menu.menu_sources, menu);
        }

        return true;
    }

    private void enterEditMode() {
        editMode = true;

        sourcesListView.setDragEnabled(true);
        sourcesListView.setAlpha(0.5f);

        // Change to edit mode title
        setTitle(getString(R.string.title_word_information_sources_settings_edit_mode));

        // Redraw menu icons
        invalidateOptionsMenu();
    }

    private void exitEditMode() {
        editMode = false;

        sourcesListView.setDragEnabled(false);
        sourcesListView.setAlpha(1.0f);

        try {
            TransactionManager.callInTransaction(getConnectionSource(), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Dao<SelectedWordInformationSource, Integer> selectedSourceDao = getHelper().getSelectedWordInformationSourceDao();

                    // Update positions of word sources
                    for (int i = 0; i < sources.size(); i++) {
                        SelectedWordInformationSource source = sources.get(i);
                        source.setPosition(i);

                        selectedSourceDao.update(source);
                    }

                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();

            Toast.makeText(this, "Error reordering sources", Toast.LENGTH_SHORT).show();
        }

        // Return to standard title
        setTitle(getString(R.string.title_word_information_sources_settings));

        // Redraw menu icons
        invalidateOptionsMenu();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.add_sources_button:
                addSources();
                return true;
            case R.id.edit_sources_button:
                enterEditMode();
                return true;
            case R.id.save_sources_button:
                exitEditMode();
                return true;
            case R.id.check_for_updates:
                checkForUpdates();
                return true;
        }

        return(super.onOptionsItemSelected(item));
    }

    private void checkForUpdates() {
        final Activity activity = this;
        SourcesUpdater updater = new SourcesUpdater(this, getHelper(), language);
        updater.start(new SourcesUpdater.SourcesUpdaterHandler() {
            @Override
            public void onSuccess(String message) {
                loadSources();
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addSources() {
        Intent addSourceIntent = new Intent(this, AddSourcesActivity.class);
        addSourceIntent.putExtra("language", language.getValue());
        startActivity(addSourceIntent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SelectedWordInformationSource source = (SelectedWordInformationSource)mArrayAdapter.getItem(position);

        Intent viewIntent = new Intent(this, EditSourceActivity.class);
        viewIntent.putExtra("selectedSourceId", source.id);
        startActivity(viewIntent);
    }

}
