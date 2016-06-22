package me.cthorne.kioku.infosources;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivityCompat;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.R;

/**
 * Created by chris on 24/01/16.
 */
public class EditSourceActivity extends OrmLiteBaseActivityCompat<DatabaseHelper> {

    private static DatabaseHelper dbHelper; // for access in fragment
    private static SelectedWordInformationSource selectedSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = getHelper();

        // Display up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get source

        int selectedSourceId = getIntent().getExtras().getInt("selectedSourceId");
        Log.d("kioku-source-edit", "source: " + selectedSourceId);

        try {
            Dao<SelectedWordInformationSource, Integer> selectedSourceDao = getHelper().getSelectedWordInformationSourceDao();

            selectedSource = selectedSourceDao.queryForId(selectedSourceId);
            if (selectedSource == null)
                throw new SQLException("SelectedWordInformationSource[" + selectedSourceId + "] not found");
            selectedSource.loadSource(getHelper());

            // Set title to source name
            setTitle(selectedSource.getSource().getTitle());

            // Display the fragment as the main content.
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new EditSourceFragment())
                    .commit();
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading source from database.", Toast.LENGTH_SHORT).show();
            Log.d("kioku-sources", "error reading source from database");
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_source, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final Context context = this;

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.delete_source_button:
                new AlertDialog.Builder(this)
                        .setTitle("Delete source")
                        .setMessage("Are you sure you want to delete this source?")
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Delete source
                                try {
                                    Dao<SelectedWordInformationSource, Integer> selectedSourceDao = getHelper().getSelectedWordInformationSourceDao();
                                    selectedSourceDao.delete(selectedSource);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                    Toast.makeText(context, "Error deleting information source.", Toast.LENGTH_SHORT).show();
                                    Log.d("kioku-sources", "error deleting information source from database");
                                }

                                finish();
                            }
                        }).show();

                return true;
        }

        return (super.onOptionsItemSelected(item));
    }

    public static class EditSourceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_word_information_source);

            final Context context = getActivity();
            final SelectedWordInformationSource selectedSource = EditSourceActivity.selectedSource;

            PreferenceScreen preferenceScreen = (PreferenceScreen)findPreference("preferenceScreen");

            CheckBoxPreference sourceEnabled = (CheckBoxPreference)preferenceScreen.findPreference("pref_source_enabled");
            sourceEnabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.d("kioku-sources", "toggled source enabled: " + newValue);

                    try {
                        selectedSource.setEnabled((Boolean) newValue);
                        Dao<SelectedWordInformationSource, Integer> selectedSourceDao = dbHelper.getSelectedWordInformationSourceDao();
                        selectedSourceDao.update(selectedSource);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error toggling enabled state of information source.", Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }

                    return true;
                }
            });

            // Default checked value
            sourceEnabled.setChecked(selectedSource.isEnabled());
        }
    }
}
