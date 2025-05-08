package me.cthorne.kioku;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import me.cthorne.kioku.auth.UserAccount;
import me.cthorne.kioku.infosources.WordInformationSource;
import me.cthorne.kioku.search.SearchJSI;
import me.cthorne.kioku.search.SearchWebViewFragment;
import me.cthorne.kioku.search.SearchWebViewFragmentAdapter;
import me.cthorne.kioku.words.Word;
import me.cthorne.kioku.words.WordInformation;
import me.cthorne.kioku.words.WordInformationType;
import me.cthorne.kioku.orm.OrmLiteBaseActivityCompat;

/**
 * Created by chris on 31/10/15.
 */
public class SearchResultsActivity extends OrmLiteBaseActivityCompat<DatabaseHelper> {

    private static final int SEARCH_WORD_RESULT_ID = 0;
    private static final String TAG = "kioku-search-results";

    public AtomicInteger pendingDownloads;
    private boolean waitingForPending;

    public SearchWebViewFragmentAdapter mAdapter;
    public ArrayList<WordInformation> wordInformations = new ArrayList<WordInformation>();
    private ViewPager mViewPager;

    public static SearchResultsActivity activeActivity;

    public int webViewLoadCounter = 0;
    public int webViewTotal = 0;

    private Word word;
    private boolean existingWord; // Is this search for an existing word? (i.e. came from WordViewActivity)

    private View progressDim;
    private ProgressBar progressBar;

    public TextView firstHint;
    public TextView secondHint;
    public TextView thirdHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        progressDim = findViewById(R.id.progress_dim);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);

        firstHint = (TextView)findViewById(R.id.first_hint);
        secondHint = (TextView)findViewById(R.id.second_hint);
        thirdHint = (TextView)findViewById(R.id.third_hint);

        // Get term to search
        String searchString = getIntent().getExtras().getString("searchTerm");

        // Get word ID for searches on existing words
        int wordId = getIntent().getExtras().getInt("wordId", 0);
        if (wordId != 0) {
            try {
                word = getHelper().getWordDao().queryForId(wordId);
                existingWord = true;
            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error searching for word", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(searchString);

        // Get array of URLs to show
        final List<WordInformationSource> sources;
        try {
            sources = getHelper().qbUserSources(true, MainActivity.currentLanguage).query();
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error retrieving sources", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Store number of URLs
        webViewTotal = sources.size();
        webViewLoadCounter = 0;

        mAdapter = new SearchWebViewFragmentAdapter(getSupportFragmentManager(), searchString, sources);

        mViewPager = (ViewPager)findViewById(R.id.pager);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(sources.size()-1); // Load all sources
        mViewPager.setPageMargin(0);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);

        if (MainActivity.isInTutorial())
            firstHint.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            // Clean up WebViews in fragments
            for (SearchWebViewFragment fragment : mAdapter.getSavedWebViewFragments().values()) {
                fragment.getWebView().destroy();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        activeActivity = this;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SEARCH_WORD_RESULT_ID) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // Handle cancellation
            } else if (resultCode == Activity.RESULT_OK) {
                existingWord = true;
                setResult(Activity.RESULT_OK);
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_word_search_results, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                handleBackPress();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.save_word_button) {
            saveWord();
            return true;
        } else if (id == R.id.action_reload_page) {
            refreshCurrentFragment();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void saveWord() {
        Log.d("kioku-save", "saveWord called");

        if (pendingDownloads.get() > 0) {
            Log.d("kioku-save", "tried to save with pending downloads; wait");
            waitingForPending = true;
            return; // Don't save until pending downloads are finished
        }

        Log.d("kioku-save", "saving word");

        // Save word in DB
        try {
            word = dbSaveWord();

            // Clear stored informations
            wordInformations.clear();

            // Show word edit
            Intent wordEditIntent = new Intent(this, WordViewActivity.class);
            wordEditIntent.putExtra("wordId", word.id);
            wordEditIntent.putExtra("searchWord", true);
            wordEditIntent.putExtra("newWord", !existingWord);
            startActivityForResult(wordEditIntent, SEARCH_WORD_RESULT_ID);
        } catch (SQLException e) {
            Toast.makeText(getApplicationContext(), "Error saving word", Toast.LENGTH_SHORT).show();

            e.printStackTrace();
        }

        // Exit select mode for fragments (in case user comes back)
        for (SearchWebViewFragment fragment : mAdapter.getSavedWebViewFragments().values()) {
            fragment.setSelectMode(false);
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                // Hide loading views
                progressDim.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private Word dbSaveWord() throws SQLException {
        final DatabaseHelper dbHelper = getHelper();
        final Dao<Word, Integer> wordDao = dbHelper.getWordDao();
        final Dao<WordInformation, Integer> wordInformationDao = dbHelper.getWordInformationDao();

        TransactionManager.callInTransaction(dbHelper.getConnectionSource(),
                new Callable<Void>() {
                    public Void call() throws Exception {
                        // note: the word may have been deleted by returning from WordView activity, so a DB lookup is required
                        if (word == null || wordDao.queryForId(word.id) == null) {
                            Log.d("kioku-search", "creating new word");

                            word = new Word();
                            word.setUserAccount(new UserAccount(KiokuServerClient.getCurrentUserId(dbHelper.getContext())));
                            Log.d("kioku-search", "cid: " + KiokuServerClient.getCurrentUserId(dbHelper.getContext()));
                            word.language = MainActivity.currentLanguage.getValue();
                            word.createdAt = new Date();

                            wordDao.create(word);
                        }

                        for (WordInformation wordInformation : wordInformations) {
                            wordInformation.setWord(word);

                            // First check the information isn't saved already
                            Where existsWhere = wordInformationDao.queryBuilder().where().eq("word_id", word.id);

                            if (wordInformation.getInformationBytes() == null)
                                existsWhere.and().isNull("informationBytes");
                            else
                                existsWhere.and().raw("cast(informationBytes AS TEXT) = ?", new SelectArg(SqlType.LONG_STRING, new String(wordInformation.getInformationBytes())));

                            if (wordInformation.getMetaInformationBytes() == null)
                                existsWhere.and().isNull("metaInformationBytes");
                            else
                                existsWhere.and().raw("cast(metaInformationBytes AS TEXT) = ?", new SelectArg(SqlType.LONG_STRING, new String(wordInformation.getMetaInformationBytes())));

                            boolean exists = existsWhere.countOf() > 0;

                            Log.d("kioku-db", "word id: " + wordInformation.getWord().id);
                            if (wordInformation.getInformationBytes() != null)
                                Log.d("kioku-db", "information: " + new String(wordInformation.getInformationBytes()));
                            if (wordInformation.getMetaInformationBytes() != null)
                                Log.d("kioku-db", "meta information: " + new String(wordInformation.getMetaInformationBytes()));

                            if (exists)
                                Log.d("kioku-db", "this word info already exists; not saving");
                            else
                                wordInformationDao.create(wordInformation);
                        }

                        return null;
                    }
                }
        );

        return word;
    }

    // User pressed save button
    private void requestSaveWord() {
        // Show loading views
        progressDim.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        // Reset saved counter
        SearchJSI.savedCount = 0;
        // Reset pending downloads counter
        pendingDownloads = new AtomicInteger();

        // Save selected data in web fragments
        for (SearchWebViewFragment fragment : mAdapter.getSavedWebViewFragments().values()) {
            if (!fragment.getWebViewClient().isLoading()) {
                fragment.saveSelected();
            } else {
                SearchJSI.savedCount++;
            }
        }

        // If all fragments have "saved" already (i.e. all are still loading) then save the word here
        if (SearchJSI.savedCount == mAdapter.getSavedWebViewFragments().size()) {
            saveWord();
        }

        // Otherwise saveWord will be called when all fragments finish saving
    }

    private void refreshCurrentFragment() {
        SearchWebViewFragment fragment = mAdapter.getSavedWebViewFragment(mViewPager.getCurrentItem());
        fragment.reload();
    }

    private void handleBackPress() {
        SearchWebViewFragment fragment = mAdapter.getSavedWebViewFragment(mViewPager.getCurrentItem());
        if (fragment.getWebView().canGoBack())
            fragment.getWebView().goBack();
        else
            finish();
    }

    public void startDownload() {
        pendingDownloads.incrementAndGet();
    }

    public void finishDownload(Bitmap imageBitmap) {
        Log.d("kioku-save", "finishDownload");

        try {
            String fileName = Utils.saveBitmapToFile(this, imageBitmap);
            // Create word information object
            WordInformation wordInformation = new WordInformation(WordInformationType.IMAGE, fileName.getBytes());
            // Store word information object to save in the database
            wordInformations.add(wordInformation);
        } catch (Exception e) {
            Log.d("kioku-js", "error saving image bitmap:" + e.getMessage());
            e.printStackTrace();
        }

        if (pendingDownloads.decrementAndGet() == 0 && waitingForPending) {
            Log.d("kioku-save", "all pending downloads finished");
            waitingForPending = false;
            saveWord();
        }
    }
}
