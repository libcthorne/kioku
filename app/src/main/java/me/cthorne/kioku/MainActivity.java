package me.cthorne.kioku;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;

import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.cthorne.kioku.auth.UserAccount;
import me.cthorne.kioku.languages.AddLanguagesActivity;
import me.cthorne.kioku.languages.LanguagesSpinnerAdapter;
import me.cthorne.kioku.languages.SelectedWordLanguage;
import me.cthorne.kioku.reminder.ReminderNotificationTimerService;
import me.cthorne.kioku.sync.Sync;
import me.cthorne.kioku.sync.SyncIntentService;
import me.cthorne.kioku.test.tests.Tests;
import me.cthorne.kioku.words.WordInformation;
import me.cthorne.kioku.words.WordLanguage;
import me.cthorne.kioku.orm.OrmLiteBaseActivityCompat;

public class MainActivity extends OrmLiteBaseActivityCompat<DatabaseHelper> {

    public static final int ADD_NEW_LANGUAGE_RESULT_ID = 60;

    // Default value for text-to-speech (true=enabled)
    private static final boolean TTS_DEFAULT_VALUE = true;

    public static MainActivity activity;
    public static DatabaseHelper dbHelper;

    // Is a sync currently in progress
    private static boolean inSync;
    // Is the current sync a result of a login
    public static boolean loginSync;
    // Did the user skip login
    public static boolean skippedLogin;

    // Views
    private GridLayout buttonGrid;

    private ImageButton searchButton;
    private RelativeLayout studyButton;
    private Spinner languageSpinner;
    private ImageButton browseButton;
    private ImageButton statisticsButton;
    private ImageButton settingsButton;
    private TextView studyItemsDueText;

    private TextView addWordsHint;
    private TextView studyWordsHint;
    private RelativeLayout finalHint;

    // TTS
    public static TextToSpeech tts;
    public static boolean ttsLoaded;
    public static boolean ttsLoading;
    public static String ttsPending;

    // Languages
    public static WordLanguage currentLanguage;
    private int currentLanguageSpinnerPosition;

    // Sync/auth

    // The authority for the sync adapter's (stub) content provider
    public static final String AUTHORITY = "me.cthorne.kioku.provider";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "me.cthorne.kioku";
    // The account name
    public static final String ACCOUNT = "dummyaccount";
    // Instance fields
    private static Account sAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Save current activity into static context
        // There is only ever one MainActivity so this should not be a problem
        activity = this;
        // Save helper into static context
        dbHelper = getHelper();

        initReminderNotifications();

        final Context context = this;

        buttonGrid = (GridLayout)findViewById(R.id.button_grid);

        languageSpinner = (Spinner)findViewById(R.id.language_spinner);

        // Initialise buttons

        searchButton = (ImageButton)findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SearchActivity.class);
                startActivity(intent);
            }
        });
        searchButton.setOnTouchListener(new ButtonTouchListener());

        studyButton = (RelativeLayout)findViewById(R.id.study_button);
        studyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, StudyActivity.class);
                startActivity(intent);
            }
        });
        studyButton.setOnTouchListener(new ButtonTouchListener());

        browseButton = (ImageButton)findViewById(R.id.browse_button);
        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, WordBrowseActivity.class);
                startActivity(intent);
            }
        });
        browseButton.setOnTouchListener(new ButtonTouchListener());

        statisticsButton = (ImageButton)findViewById(R.id.statistics_button);
        statisticsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, StatisticsActivity.class);
                startActivity(intent);
            }
        });
        statisticsButton.setOnTouchListener(new ButtonTouchListener());

        settingsButton = (ImageButton)findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SettingsActivity.class);
                startActivity(intent);
            }
        });
        settingsButton.setOnTouchListener(new ButtonTouchListener());

        studyItemsDueText = (TextView)findViewById(R.id.study_items_due_count);

        addWordsHint = (TextView)findViewById(R.id.add_words_hint);
        studyWordsHint = (TextView)findViewById(R.id.study_words_hint);
        finalHint = (RelativeLayout)findViewById(R.id.final_hint);

        // TTS
        //Log.d("kioku-tts", "init tts");
        //ttsLoad();

        // Create the dummy account
        sAccount = CreateSyncAccount(this);

        // Check currently logged in account is valid
        if (KiokuServerClient.isLoggedIn(this)) {
            try {
                Dao<UserAccount, Integer> userAccountDao = getHelper().getUserAccountDao();

                if (!userAccountDao.idExists(KiokuServerClient.getCurrentUserId(this))) {
                    Log.d("kioku-main", "logged in account is not valid. logging out.");
                    KiokuServerClient.setLoggedIn(this, null, null, null, false, null); // logout
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Log.d("kioku-main", "error verifying current account");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ttsShutdown();
    }

    @Override
    public void onResume() {
        super.onResume();

        onDbChange();

        if (isFinishing())
            return; // Activity may finish here if the user has no languages

        // Sync automatically on login
        //if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("loginSync", false)) {
        if (MainActivity.loginSync) {
            Log.d("kioku-main", "user logged in; do sync");
            requestSync(this);
            loginSync = false;
        } else {
            Log.d("kioku-main", "onResume; no sync");
        }

        // Save skip login so user isn't shown login screen from now on

        if (MainActivity.skippedLogin) {
            Log.d("kioku-login", "set skippedLogin to true");
            KiokuServerClient.getPreferences(this).edit().putBoolean("skippedLogin", true).commit();
            skippedLogin = false;
            LoginActivity.activity.finish(); // prevent going back once skip is complete
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sync_button) {
            // Original logic for sync_button
            requestSync(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("kioku-languages", "onActivityResult " + requestCode  +"," + resultCode);
        if (requestCode == ADD_NEW_LANGUAGE_RESULT_ID && resultCode == Activity.RESULT_OK) {
            try {
                Log.d("kioku-languages", "new language added");
                List<SelectedWordLanguage> languages = dbHelper.getUserLanguages();
                setCurrentLanguage(languages.get(languages.size() - 1).getLanguage()); // select last language
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void setCurrentLanguage(WordLanguage language) {
        currentLanguage = language;
        KiokuServerClient.getPreferences(this).edit().putInt("currentLanguage", currentLanguage.getValue()).commit();
    }

    /**
     * Called when the user selects a language from the language spinner.
     * @param language
     */
    private void onSelectLanguage(WordLanguage language) {
        // Do nothing if the language was already selected
        if (currentLanguage == language)
            return;

        Log.d("kioku-language", "selected language " + language);

        setCurrentLanguage(language);

        // TTS needs to be reloaded
        ttsReload();

        // Refresh to-study count
        checkTutorialStage();
        loadToStudyCount();
    }

    public static boolean isInTutorial() {
        if (inSync)
            return false; // another check will be performed after sync is finished

        SharedPreferences prefs = KiokuServerClient.getPreferences(activity);

        if (!prefs.contains("inTutorial")) {
            // If the user hasn't done the tutorial here but already has words
            // then they don't need to see it
            try {
                if (dbHelper.qbUserWordInformations(false, true).countOf() > 0) {
                    prefs.edit().putBoolean("inTutorial", false).commit();
                    return false;
                } else {
                    prefs.edit().putBoolean("inTutorial", true).commit();
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return prefs.getBoolean("inTutorial", true);
    }

    public static void setInTutorial(boolean inTutorial) {
        KiokuServerClient.getPreferences(activity).edit().putBoolean("inTutorial", inTutorial).commit();
    }


    private void onDbChange() {
        loadLanguagesSpinner();

        if (currentLanguage != null) { // currentLanguage may be null at activity creation
            checkTutorialStage();
            loadToStudyCount();
        }
    }

    private void checkTutorialStage() {
        try {
            long wordInformationCount = getHelper().qbUserWordInformations(false, true).countOf();

            /*if (wordInformationCount == 0)
                addWordsHint.setVisibility(View.VISIBLE);
            else
                addWordsHint.setVisibility(View.GONE);*/

            if (isInTutorial()) {
                // Has user done a quad test?
                if (KiokuServerClient.getPreferences(this).getBoolean("quadHintSeen", false)) {
                    addWordsHint.setVisibility(View.GONE);
                    studyWordsHint.setVisibility(View.GONE);
                    finalHint.setVisibility(View.VISIBLE);

                    buttonGrid.setAlpha(0.5f);

                    finalHint.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            buttonGrid.setAlpha(1.0f);
                            finalHint.setVisibility(View.GONE);

                            setInTutorial(false);

                            return true;
                        }
                    });
                } else {
                    if (wordInformationCount == 0) {
                        addWordsHint.setVisibility(View.VISIBLE);
                        studyWordsHint.setVisibility(View.GONE);
                        finalHint.setVisibility(View.GONE);
                    } else {
                        addWordsHint.setVisibility(View.GONE);
                        studyWordsHint.setVisibility(View.VISIBLE);
                        finalHint.setVisibility(View.GONE);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadToStudyCount() {
        final long due = Tests.countDueTests(getHelper());

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                activity.studyItemsDueText.setText(due > 0 ? String.valueOf(due) : "✓");
            }
        });
    }

    private void loadLanguagesSpinner() {
        final Context context = this;

        if (!checkUserHasSelectedAtLeastOneLanguage())
            return;

        // Get current language
        currentLanguage = WordLanguage.fromInt(KiokuServerClient.getPreferences(this).getInt("currentLanguage", -1));

        try {
            List<WordLanguage> languages = new ArrayList<>();
            int position = 0;
            currentLanguageSpinnerPosition = 0;
            for (SelectedWordLanguage selectedLanguage : getHelper().getUserLanguages()) {
                if (selectedLanguage.getLanguage() == currentLanguage)
                    currentLanguageSpinnerPosition = position;

                position++;
                languages.add(selectedLanguage.getLanguage());
            }

            final LanguagesSpinnerAdapter adapter = new LanguagesSpinnerAdapter(this, R.layout.language_spinner_item, languages);

            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            languageSpinner.setAdapter(adapter);
            // Listen for selects
            languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.d("kioku-language", "selected " + position + "/" + adapter.getCount() + "," + languageSpinner.getSelectedItemPosition());
                    if (position == adapter.getCount() - 1) {
                        // + Add language
                        Intent addLanguageIntent = new Intent(context, AddLanguagesActivity.class);
                        addLanguageIntent.putExtra("fromMain", true);
                        startActivityForResult(addLanguageIntent, ADD_NEW_LANGUAGE_RESULT_ID);

                        // Keep the previous language "selected" - don't want to show "+ Add language" in spinner box
                        languageSpinner.setSelection(currentLanguageSpinnerPosition);
                    } else {
                        WordLanguage selectedLanguage = (WordLanguage)languageSpinner.getSelectedItem();
                        onSelectLanguage(selectedLanguage);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            // Select current language
            languageSpinner.setSelection(currentLanguageSpinnerPosition);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean checkUserHasSelectedAtLeastOneLanguage() {
        try {
            if (getHelper().countUserLanguages() > 0) {
                Log.d("kioku-main", "User has languages");
                return true;  // If user has languages, nothing to do
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Otherwise change to add language activity

        Intent addLanguageIntent = new Intent(this, AddLanguagesActivity.class);
        startActivity(addLanguageIntent);
        finish();
        return false;
    }

    /**
     * Start the reminder notification service.
     * Services can only be started once, so nothing will happen if
     * it was already started by the boot receiver.
     * The boot receiver might miss cases such as initial installation, which
     * is why the service can be started here too.
     */
    private void initReminderNotifications() {
        Intent reminderNotificationServiceIntent = new Intent(this, ReminderNotificationTimerService.class);
        startService(reminderNotificationServiceIntent);
    }

    private static void ttsLoad() {
        Log.d("kioku-tts", "ttsLoad");

        boolean ttsEnabled = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("pref_tts_enabled", TTS_DEFAULT_VALUE);
        if (!ttsEnabled)
            return;

        if (ttsLoading || ttsLoaded)
            return;

        ttsLoaded = false;
        ttsLoading = true;
        tts = new TextToSpeech(MainActivity.activity, new TextToSpeech.OnInitListener() {
            @Override
            @SuppressWarnings("deprecation")
            public void onInit(int status) {
                ttsLoading = false;

                if (status == TextToSpeech.ERROR) {
                    Log.e("kioku-tts", "error initiating tts");
                    return;
                }

                Log.d("kioku-tts", "tts initiated");

                Locale locale = currentLanguage.getLocale();
                tts.setLanguage(locale);
                if (tts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                    tts.setLanguage(locale);
                } else {
                    Log.d("kioku-tts", "Error SetLocale (" + locale.toString() + ")");
                    tts.shutdown();
                    return;
                }
                ttsLoaded = true;

                if (ttsPending != null) { // for any requests received during initialisation

                    if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        tts.speak(ttsPending, TextToSpeech.QUEUE_FLUSH, null, "tts");
                    } else {
                        tts.speak(ttsPending, TextToSpeech.QUEUE_FLUSH, null);
                    }

                    ttsPending = null;
                }
            }
        });
    }

    private static void ttsShutdown() {
        if (ttsLoaded) {
            Log.d("kioku-tts", "shutdown tts");

            ttsLoaded = false;
            tts.shutdown();
        }
    }

    private void ttsReload() {
        Log.d("kioku-tts", "reload tts");
        ttsShutdown();
        ttsLoad();
    }

    public static void ttsStop() {
        if (ttsLoaded)
            tts.stop();
    }

    @SuppressWarnings("deprecation")
    public static void ttsSpeak(String message) {
        boolean ttsEnabled = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("pref_tts_enabled", TTS_DEFAULT_VALUE);
        if (!ttsEnabled)
            return;

        Log.d("kioku-tts", "ttsSpeak: " + message);

        if (!ttsLoaded && !ttsLoading)
            ttsLoad();

        if (ttsLoaded) {
            Log.d("kioku-tts", "immediate");

            if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "tts");
            } else {
                tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
            }
        } else {
            Log.d("kioku-tts", "pending");
            ttsPending = message;
        }
    }

    public static void ttsSpeak(WordInformation wordInformation) {
        String ttsString = null;

        switch (wordInformation.getInformationType()) {
            case WORD_FORM:
                if (wordInformation.getMetaInformationBytes() != null)
                    ttsString = new String(wordInformation.getMetaInformationBytes()); // use kana if available
                else if (wordInformation.getInformationBytes() != null)
                    ttsString = new String(wordInformation.getInformationBytes()); // otherwise use kanji and hope TTS gets it right
                break;
            case IMAGE:
                ttsString = wordInformation.getWord().getWordStringKanaPreferred(MainActivity.dbHelper);
                break;
            case SENTENCE:
                ttsString = new String(wordInformation.getInformationBytes());
                break;
            default:
                throw new InvalidParameterException("ttsSpeak does not support this type of word information");
        }

        if (ttsString != null)
            ttsSpeak(ttsString);
    }

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            Log.e("kioku-auth", "created dummy account");
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */

            Log.e("kioku-auth", "error creating dummy account");
        }

        return newAccount;
    }

    public static ProgressDialog syncDialog;
    public static Handler syncDialogHandler;
    public static Messenger syncMessenger;

    public static void requestSync(Context context) {
        if (!KiokuServerClient.isLoggedIn(context)) {
            Intent loginIntent = new Intent(context, LoginActivity.class);
            context.startActivity(loginIntent);
            Toast.makeText(context, "You need to login to use sync", Toast.LENGTH_SHORT).show();
            return;
        }

        inSync = true;

        // Create/show sync progress dialog

        syncDialog = new ProgressDialog(context);
        syncDialog.setTitle("Sync");
        syncDialog.setMessage("Connecting to server");
        syncDialog.setCancelable(false);
        syncDialog.setIndeterminate(true);
        syncDialog.show();

        syncDialogHandler = new SyncDialogHandler(context);

        // Start sync

        syncMessenger = new Messenger(syncDialogHandler);

        Intent syncIntent = new Intent(context, SyncIntentService.class);
        syncIntent.putExtra("messenger", syncMessenger);
        context.startService(syncIntent);

        /*
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true); // sync immediately
        ContentResolver.requestSync(sAccount, AUTHORITY, settingsBundle);
        */
    }

    private static class SyncDialogHandler extends Handler {
        private Context context;

        public SyncDialogHandler(Context context) {
            this.context = context;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Sync.MESSAGE_ERROR:
                    hideSyncDialog();
                    Toast.makeText(context, (String)msg.obj, Toast.LENGTH_SHORT).show();
                    if (Sync.authError) {
                        Toast.makeText(context, "Please login to use sync", Toast.LENGTH_SHORT).show();

                        try {
                            KiokuServerClient.setLoggedIn(context, null, null, null, false, null); // logout
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        Intent loginIntent = new Intent(context, LoginActivity.class);
                        context.startActivity(loginIntent);
                    }
                    break;
                case Sync.MESSAGE_PROGRESS:
                    updateSyncDialog((String)msg.obj);
                    break;
                case Sync.MESSAGE_FINISHED:
                    hideSyncDialog();
                    MainActivity.activity.onDbChange();
                    Toast.makeText(context, "Sync complete", Toast.LENGTH_SHORT).show();
                    break;
                case Sync.MESSAGE_FINISHED_NO_CHANGES:
                    hideSyncDialog();
                    Toast.makeText(context, "Nothing to sync", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        private void updateSyncDialog(String message) {
            if (syncDialog != null)
                syncDialog.setMessage(message);
        }

        private void hideSyncDialog() {
            inSync = false;

            if (syncDialog != null && syncDialog.isShowing())
                syncDialog.dismiss();
            else
                Log.d("kioku-sync", "sync dialog already closed");
        }
    }

    // Rect checking adapted from http://stackoverflow.com/a/8069887/5402565
    private class ButtonTouchListener implements View.OnTouchListener {
        private Rect rect; // Bounds of the view's bounds

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setScaleX(0.95f);
                    v.setScaleY(0.95f);
                    v.setAlpha(0.9f);
                    // Construct a rect of the view's bounds
                    rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY()))
                        break;
                case MotionEvent.ACTION_UP:
                    v.setScaleX(1);
                    v.setScaleY(1);
                    v.setAlpha(1);
                    break;
            }

            return false;
        }
    }
}
