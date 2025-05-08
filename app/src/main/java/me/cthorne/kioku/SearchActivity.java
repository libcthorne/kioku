package me.cthorne.kioku;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import me.cthorne.kioku.orm.OrmLiteBaseActivityCompat;

import org.joda.time.DateTime;
import org.joda.time.Period;

import java.sql.SQLException;
import java.util.List;

import me.cthorne.kioku.infosources.SourcesActivity;
import me.cthorne.kioku.infosources.SourcesUpdater;
import me.cthorne.kioku.infosources.WordInformationSource;
import me.cthorne.kioku.words.Word;

public class SearchActivity extends OrmLiteBaseActivityCompat<DatabaseHelper> {

    public static final int WORD_SEARCH_RESULT_ID = 50;
    public static final int ADD_SOURCES_RESULT_ID = 51;
    private static final int MANUAL_EDIT_SOURCES_RESULT_ID = 52;

    // How often to check for updates to sources (in hours)
    // TODO: add a preference for this
    private static final int SOURCES_UPDATE_INTERVAL_HR = 1;

    // Used to prevent double source check when searching from other app
    private static boolean addingSources;

    private Button clearButton;
    private Button searchButton;
    private EditText mainEditText;

    private TextView searchHint;

    private boolean inSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        clearButton = (Button) findViewById(R.id.main_edittext_clear);
        clearButton.setBackgroundColor(Color.TRANSPARENT);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainEditText.setText("");
            }
        });

        searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inSearch)
                    return; // ignore events after search is started

                startSearch();
            }
        });

        mainEditText = (EditText) findViewById(R.id.main_edittext);
        mainEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (inSearch)
                    return false; // ignore events after search is started

                if (actionId == EditorInfo.IME_ACTION_SEARCH || // search button on keyboard pressed
                        ((event.getAction() == KeyEvent.ACTION_DOWN) && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) // enter button pressed
                    return !startSearch(); // false closes the keyboard; only return false on success

                return false;
            }
        });

        searchHint = (TextView)findViewById(R.id.search_hint);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                mainEditText.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
                startSearch();
            }
        }

        // First update sources
        /*Intent updateSourcesIntent = new Intent(this, SourcesActivity.class);
        updateSourcesIntent.putExtra("language", MainActivity.currentLanguage.getValue());
        updateSourcesIntent.putExtra("updateOnly", true);
        startActivity(updateSourcesIntent);
        overridePendingTransition(0, 0);*/

        // First update sources if time since last update exceeds limit
        final SharedPreferences preferences = KiokuServerClient.getPreferences(this);

        DateTime lastSourcesUpdate = new DateTime(preferences.getLong("lastSourcesUpdate", 0));
        DateTime now = new DateTime();
        Period p = new Period(now, lastSourcesUpdate);

        if (p.getHours() > SOURCES_UPDATE_INTERVAL_HR) {
            SourcesUpdater updater = new SourcesUpdater(this, getHelper(), MainActivity.currentLanguage);
            updater.start(new SourcesUpdater.SourcesUpdaterHandler() {
                @Override
                public void onSuccess(String message) {
                    Log.d("kioku-search", "sources updated: " + message);
                }

                @Override
                public void onError(String message) {
                    Log.d("kioku-search", "sources error: " + message);
                }
            }, false);
        } else {
            Log.d("kioku-search", "update checked for within last hour");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("kioku-search", "onresume searchactivity");

        if (!checkSources())
            return;

        searchHint.setVisibility(MainActivity.isInTutorial() ? View.VISIBLE : View.GONE);

        inSearch = false;

        // Select edit and show keyboard
        mainEditText.requestFocus();
        Utils.showKeyboard(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        inSearch = true;

        // Hide keyboard
        Utils.hideKeyboard(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("kioku-search", "onActivityResult searchactivity " + requestCode + "/" + resultCode);

        if (requestCode == WORD_SEARCH_RESULT_ID && resultCode == Activity.RESULT_OK) {
            finish();
        } else if (requestCode == ADD_SOURCES_RESULT_ID) {
            addingSources = false;

            try {
                List<WordInformationSource> sources = getHelper().qbUserSources(true, MainActivity.currentLanguage).query();

                // User tried to search but didn't have sources
                if (sources.size() == 0) {
                    // They were redirected to add some but chose not to
                    // If they come back, the activity should finish rather than ask them to choose sources again
                    finish();
                } else {
                    // If they chose some and a search string is present, search immediately
                    String searchTerm = mainEditText.getText().toString();
                    if (searchTerm.length() > 0)
                        startSearch();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error getting sources", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == MANUAL_EDIT_SOURCES_RESULT_ID) {
            try {
                List<WordInformationSource> sources = getHelper().qbUserSources(true, MainActivity.currentLanguage).query();

                // User has just finished manually editing their sources
                if (sources.size() == 0) {
                    // They no longer have any sources, so exit search; no error as this was done willingly
                    finish();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error getting sources", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_word_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.information_sources_settings) {
            Intent intent = new Intent(this, SourcesActivity.class);
            intent.putExtra("language", MainActivity.currentLanguage.getValue());
            startActivityForResult(intent, MANUAL_EDIT_SOURCES_RESULT_ID);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean startSearch() {
        String searchTerm = mainEditText.getText().toString();

        if (searchTerm.length() == 0) {
            Toast.makeText(this, "Please enter a word to search", Toast.LENGTH_SHORT).show();
            return false;
        }

        searchWord(searchTerm);
        return true;
    }

    private static boolean checkSources(Activity activity, DatabaseHelper dbHelper) {
        if (addingSources)
            return false;

        Log.d("kioku-search", "[s] checkSources");

        try {
            List<WordInformationSource> sources = dbHelper.qbUserSources(true, MainActivity.currentLanguage).query();

            if (sources.size() == 0) {
                if (!MainActivity.isInTutorial())
                    Toast.makeText(activity, R.string.search_no_sources_error, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(activity, SourcesActivity.class);
                intent.putExtra("language", MainActivity.currentLanguage.getValue());
                intent.putExtra("fromSearch", true);
                activity.startActivityForResult(intent, ADD_SOURCES_RESULT_ID);

                addingSources = true;

                return false;
            } else {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkSources() {
        Log.d("kioku-search", "checkSources");

        return checkSources(this, getHelper());
    }

    public static void searchWord(Activity activity, DatabaseHelper dbHelper, String searchTerm, Word word) {
        if (!checkSources(activity, dbHelper))
            return;

        Intent searchIntent = new Intent(activity, SearchResultsActivity.class);
        searchIntent.putExtra("searchTerm", searchTerm);
        searchIntent.putExtra("wordId", word != null ? word.id : 0);
        activity.startActivityForResult(searchIntent, WORD_SEARCH_RESULT_ID);
        //activity.startActivity(searchIntent);
    }

    private void searchWord(String searchTerm) {
        searchWord(this, getHelper(), searchTerm, null);
    }

}
