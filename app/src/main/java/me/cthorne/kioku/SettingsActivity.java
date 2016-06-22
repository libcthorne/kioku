package me.cthorne.kioku;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivityCompat;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;

import java.io.File;
import java.sql.SQLException;

import me.cthorne.kioku.languages.SelectedWordLanguage;
import me.cthorne.kioku.settings.LanguageSettingsActivity;
import me.cthorne.kioku.test.WordInformationTestAnswer;
import me.cthorne.kioku.test.WordInformationTestPerformance;
import me.cthorne.kioku.words.Word;

/**
 * Created by chris on 18/12/15.
 */
public class SettingsActivity extends OrmLiteBaseActivityCompat<DatabaseHelper> {

    private static DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = getHelper();

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            final Context context = getActivity();

            PreferenceScreen preferenceScreen = (PreferenceScreen)findPreference("preferenceScreen");

            // Language specific preferences

            PreferenceCategory languagePreferences = (PreferenceCategory)findPreference("pref_languages");

            try {
                for (final SelectedWordLanguage language : dbHelper.getUserLanguages()) {
                    Preference userLanguagePreference = new Preference(context);
                    userLanguagePreference.setTitle(language.getLanguage().toString());

                    userLanguagePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {

                            Intent languagePreferencesIntent = new Intent(context, LanguageSettingsActivity.class);
                            languagePreferencesIntent.putExtra("language", language.getLanguage().getValue());
                            startActivity(languagePreferencesIntent);

                            return false;

                        }
                    });

                    languagePreferences.addPreference(userLanguagePreference);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(context, "Error adding language preferences", Toast.LENGTH_SHORT).show();
                languagePreferences.setEnabled(false);
            }

            // Account preferences

            // For users signed in
            //PreferenceCategory syncPreferences = (PreferenceCategory)findPreference("pref_sync");
            PreferenceCategory signedInAccountPreferences = (PreferenceCategory)findPreference("pref_account_settings_signed_in");
            // For users not signed in
            PreferenceCategory notSignedInAccountPreferences = (PreferenceCategory)findPreference("pref_account_settings_not_signed_in");

            if (KiokuServerClient.isLoggedIn(context)) { // preferences for if logged in
                preferenceScreen.removePreference(notSignedInAccountPreferences);

                // Manage account

                Preference manageAccount = findPreference("pref_account_manage");
                manageAccount.setSummary(KiokuServerClient.getPreferences(context).getString("email", "")); // Set summary to current account's email
                /*manageAccount.setSummary(KiokuServerClient.getPreferences(context).getString("email", "") +
                        " (" + KiokuServerClient.getPreferences(context).getInt("currentUserId", -1) + ")"); // Set summary to current account's email*/

                // Switch account

                Preference switchAccount = findPreference("pref_account_switch");
                switchAccount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {

                        new AlertDialog.Builder(context)
                                .setTitle("Switch account")
                                .setMessage("Are you sure you want to switch account? You will be logged out.")
                                .setNegativeButton(android.R.string.no, null)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            // clear saved details
                                            KiokuServerClient.setLoggedIn(context, dbHelper, "", "", false, new KiokuServerClient.LoginHandler() {
                                                @Override
                                                public void onFinish() {
                                                    Intent intent = new Intent(context, LoginActivity.class);
                                                    startActivity(intent);
                                                    ((Activity)context).finish();
                                                }
                                            });
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).create().show();

                        return false;
                    }
                });

                // Sync

                /*Preference forceSync = findPreference("pref_force_sync");
                forceSync.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        MainActivity.requestSync(context);

                        return false;
                    }
                });*/

                Preference forceFullSync = findPreference("pref_force_clean_sync");
                forceFullSync.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {

                        new AlertDialog.Builder(context)
                                .setTitle("Clean sync")
                                .setMessage("Are you sure you want to do a clean sync? Any changes you made since the last sync will be lost.")
                                .setNegativeButton(android.R.string.no, null)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Clear all synced local tables first
                                        try {
                                            TableUtils.clearTable(dbHelper.getConnectionSource(), Word.class);

                                            // Sync
                                            //KiokuSync.startSync(context);
                                            MainActivity.requestSync(context);

                                            // Delete local media
                                            File mediaDir = Utils.mediaDir(context);
                                            File[] mediaFiles = mediaDir.listFiles();
                                            if (mediaFiles != null) {
                                                for (File mediaFile : mediaFiles) {
                                                    if (mediaFile.delete())
                                                        Log.d("kioku-reset", "deleted " + mediaFile.getName());
                                                    else
                                                        Log.e("kioku-reset", "error deleting " + mediaFile.getName());
                                                }
                                            } else {
                                                Log.d("kioku-reset", "no media files found to delete");
                                            }
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).create().show();

                        return false;
                    }
                });

            } else { // preferences for if not logged in
                //preferenceScreen.removePreference(syncPreferences);
                preferenceScreen.removePreference(signedInAccountPreferences);

                Preference createAccount = findPreference("pref_account_create");
                createAccount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        try {
                            KiokuServerClient.setLoggedIn(context, null, null, null, false, null); // logout
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        Intent intent = new Intent(context, RegisterActivity.class);
                        startActivity(intent);

                        return false;
                    }
                });

                Preference loginAccount = findPreference("pref_account_login");
                loginAccount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        try {
                            KiokuServerClient.setLoggedIn(context, null, null, null, false, null); // logout
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);

                        return false;
                    }
                });
            }

            // Help preferences

            /*Preference seeTutorialButton = findPreference("pref_see_tutorial");
            seeTutorialButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(context, TutorialActivity.class);
                    intent.putExtra("manualStart", true);
                    startActivity(intent);

                    return false;
                }
            });*/

            // Developer preferences

            Preference resetNextDueButton = findPreference("pref_dev_reset_next_due");
            resetNextDueButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(context)
                            .setTitle("Reset next due")
                            .setMessage("Are you sure you want to reset all tests to be due for now?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        Dao<WordInformationTestPerformance, Integer> wordInformationTestPerformanceDao = dbHelper.getWordInformationTestPerformanceDao();

                                        for (WordInformationTestPerformance performance : wordInformationTestPerformanceDao.queryForAll()) {
                                            performance.resetRepetitionValues();

                                            Log.d("kioku-reset", "nextDue: " + performance.getNextDue());

                                            wordInformationTestPerformanceDao.update(performance);
                                        }
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).create().show();

                    return true;
                }
            });

            Preference resetTestsButton = findPreference("pref_dev_reset_tests");
            resetTestsButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(context)
                            .setTitle("Delete test information")
                            .setMessage("Are you sure you want to delete all test information?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        TableUtils.clearTable(dbHelper.getConnectionSource(), WordInformationTestPerformance.class);
                                        TableUtils.clearTable(dbHelper.getConnectionSource(), WordInformationTestAnswer.class);
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).create().show();

                    return true;
                }
            });

            Preference unseeIntroButton = findPreference("pref_unsee_intro");
            unseeIntroButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    KiokuServerClient.getPreferences(context).edit().putBoolean("seenIntro", false).commit();
                    return true;
                }
            });

            Preference unseeHintsButton = findPreference("pref_unsee_hints");
            unseeHintsButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    KiokuServerClient.getPreferences(context).edit().putBoolean("quadHintSeen", false).putBoolean("matchHintSeen", false).commit();
                    return true;
                }
            });
        }
    }

    public static class ManageAccountSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_manage_account);
        }
    }


}
