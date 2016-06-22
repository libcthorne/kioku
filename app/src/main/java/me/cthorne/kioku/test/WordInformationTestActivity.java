package me.cthorne.kioku.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivityCompat;

import im.delight.android.audio.SoundManager;
import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.R;
import me.cthorne.kioku.WordViewActivity;
import me.cthorne.kioku.test.tests.Tests;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 07/01/16.
 */
public class WordInformationTestActivity extends OrmLiteBaseActivityCompat<DatabaseHelper> {

    private SoundManager soundManager;
    private WordInformationTest test;

    private static Activity activity;

    public static WordInformation currentWordInformation;

    private void startActiveTest() {
        test = Tests.getActiveTest();

        Log.d("kioku-test", "startActiveTest");

        if (test == null) {
            // If we've reached the last test, finish
            Log.d("kioku-test", "startActiveTest: no more tests");
            finish();

            return;
        }

        // Try next test if this one can't be started
        if (!test.canStartTest(getHelper())) {
            Log.d("kioku-test", "startActiveTest: can't start this test");
            Tests.proceedToNextTest(getHelper());
            startActiveTest();
            return;
        }

        Log.d("kioku-test", "startActiveTest: starting " + test);

        // Start test
        setContentView(test.getContentView());
        test.setTestActivity(this); // Pass in activity for test to use
        test.startTest(getHelper(), new WordInformationTest.OnTestFinishedHandler() {
            @Override
            public void onFinish() {
                // finished handler
                Tests.proceedToNextTest(getHelper());
                startActiveTest();
            }
        });

        // Set title to test type
        getSupportActionBar().setTitle(Tests.getActiveTestType().toName());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WordInformationTestActivity.activity = this;
    }

    @Override
    protected void onResume() {
        super.onResume();

        int maxSimultaneousStreams = 3;
        soundManager = new SoundManager(this, maxSimultaneousStreams);
        soundManager.start();
        soundManager.load(R.raw.right1);
        soundManager.load(R.raw.wrong1);

        startActiveTest();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (test != null) {
            test.resetTest();
        }

        if (soundManager != null) {
            soundManager.cancel();
            soundManager = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_word_information_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_word_button:
                if (currentWordInformation != null) {
                    Intent wordViewIntent = new Intent(this, WordViewActivity.class);
                    wordViewIntent.putExtra("wordId", currentWordInformation.getWord().id);
                    startActivity(wordViewIntent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void playCorrectSound() {
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_sfx_enabled", true))
            return;

        soundManager.play(R.raw.right1);
    }

    public void playIncorrectSound() {
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_sfx_enabled", true))
            return;

        soundManager.play(R.raw.wrong1);
    }

    public static void finishDbError() {
        Toast.makeText(activity, "Database error", Toast.LENGTH_SHORT).show();
        activity.finish();
    }
}
