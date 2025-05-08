package me.cthorne.kioku.languages;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.KiokuServerClient;
import me.cthorne.kioku.MainActivity;
import me.cthorne.kioku.R;
import me.cthorne.kioku.words.WordLanguage;
import me.cthorne.kioku.orm.OrmLiteBaseActivityCompat;

/**
 * Created by chris on 06/02/16.
 */
public class AddLanguagesActivity extends OrmLiteBaseActivityCompat<DatabaseHelper> {

    private ArrayList<SelectableWordLanguage> languages;
    private ListView languagesListView;
    private AddLanguagesListViewAdapter mArrayAdapter;
    private boolean fromMain; // was this activity started from MainActivity?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_language);

        fromMain = getIntent().getBooleanExtra("fromMain", false);

        DatabaseHelper dbHelper = getHelper();

        try {
            Dao<SelectedWordLanguage, Integer> selectedWordLanguageDao = dbHelper.getSelectedWordLanguageDao();

            // List of all word languages
            languages = new ArrayList<>();

            for (WordLanguage language : WordLanguage.values()) {
                SelectableWordLanguage selectableWordLanguage = new SelectableWordLanguage(language);

                // check if user has selected the current language already
                boolean languageSelected = selectedWordLanguageDao.queryBuilder()
                        .where().eq("userAccount_id", KiokuServerClient.getCurrentUserId(this))
                        .and().eq("language", language.getValue()).queryForFirst() != null;

                selectableWordLanguage.setSelected(languageSelected);

                Log.d("kioku-languages", "onCreate: languageSelected = " + selectableWordLanguage.isSelected() + " for " + language);

                languages.add(selectableWordLanguage);
            }

            // Adapter for user words list view
            mArrayAdapter = new AddLanguagesListViewAdapter(this, languages);

            // List view
            languagesListView = (ListView)findViewById(R.id.languages_listview);
            languagesListView.setAdapter(mArrayAdapter);
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error retrieving languages", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_languages, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_languages_button) {
            saveSelectedLanguages();
            return true;
        }
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveSelectedLanguages() {
        Log.d("kioku-test", "saving languages");

        DatabaseHelper dbHelper = getHelper();

        try {
            Dao<SelectedWordLanguage, Integer> selectedWordLanguageDao = dbHelper.getSelectedWordLanguageDao();

            boolean languageSelected = false;
            for (SelectableWordLanguage language : languages) {
                Log.d("kioku-languages", language.getLanguage().toString() + ": " + language.isSelected());

                // Remember if a selected language is found
                if (language.isSelected() && !languageSelected)
                    languageSelected = true;

                SelectedWordLanguage selectedLanguage = selectedWordLanguageDao.queryBuilder()
                        .where().eq("userAccount_id", KiokuServerClient.getCurrentUserId(this))
                        .and().eq("language", language.getLanguage().getValue()).queryForFirst();

                if (language.isSelected()) {
                    // Create selected language if necessary
                    if (selectedLanguage == null) {
                        selectedLanguage = new SelectedWordLanguage(this, language.getLanguage());
                        selectedWordLanguageDao.create(selectedLanguage);

                        // notify caller that a new language was selected
                        setResult(Activity.RESULT_OK);

                        Log.d("kioku-languages", "language " + language.getLanguage() + " newly selected");
                    } else {
                        Log.d("kioku-languages", "language " + language.getLanguage() + " was selected");
                    }
                } else {
                    // Delete selected language if necessary
                    if (selectedLanguage != null) {
                        selectedWordLanguageDao.delete(selectedLanguage);

                        Log.d("kioku-languages", "language " + language.getLanguage() + " deleted");
                    } else {
                        Log.d("kioku-languages", "language " + language.getLanguage() + " not selected and wasn't previously selected either");
                    }
                }
            }

            if (languageSelected)
                exitToMain();
            else
                Toast.makeText(this, R.string.add_languages_none_selected_error, Toast.LENGTH_SHORT).show();
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving languages", Toast.LENGTH_SHORT).show();
        }
    }

    private void exitToMain() {
        if (!fromMain) {
            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);
        }

        finish();
    }

}
