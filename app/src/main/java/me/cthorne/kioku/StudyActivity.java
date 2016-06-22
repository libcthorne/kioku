package me.cthorne.kioku;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivityCompat;

import java.util.ArrayList;
import java.util.Map;

import me.cthorne.kioku.test.TestTypeButton;
import me.cthorne.kioku.test.WordInformationTest;
import me.cthorne.kioku.test.WordInformationTestActivity;
import me.cthorne.kioku.test.WordInformationTestType;
import me.cthorne.kioku.test.tests.Tests;

/**
 * Created by chris on 17/11/15.
 */
public class StudyActivity extends OrmLiteBaseActivityCompat<DatabaseHelper> {

    private ArrayList<TestTypeButton> testTypeButtons;

    private TextView firstHint;
    private TextView secondHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firstHint = (TextView) findViewById(R.id.first_hint);
        secondHint = (TextView) findViewById(R.id.second_hint);

        // Hide any study notifications
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        // Create test type buttons

        testTypeButtons = new ArrayList<>();

        LinearLayout wordInformationTests = (LinearLayout)findViewById(R.id.word_information_tests);

        Map<WordInformationTestType, ArrayList<WordInformationTest>> tests = Tests.getAll();

        for (final WordInformationTestType testType : WordInformationTestType.values()) {
            if (tests.get(testType) == null)
                continue;

            Button startTestButton = new Button(this);
            startTestButton.setText(testType.toName());
            startTestButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            wordInformationTests.addView(startTestButton);

            final Context context = this;
            startTestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tests.setActiveTestType(testType);

                    // Make sure old current word infos aren't carried over when starting new test
                    WordInformationTestActivity.currentWordInformation = null;

                    Intent searchIntent = new Intent(context, WordInformationTestActivity.class);
                    searchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(searchIntent);
                }
            });

            // Store button with test type
            testTypeButtons.add(new TestTypeButton(startTestButton, testType));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean foundStartableTest = false;

        // Enable/disable test start buttons
        for (TestTypeButton testTypeButton : testTypeButtons) {
            Button button = testTypeButton.getButton();
            WordInformationTestType testType = testTypeButton.getTestType();

            // Enable button only if test type can be started
            long due = Tests.countDueForTestType(testType, getHelper());
            button.setEnabled(due > 0);

            button.setText(testType.toName() + " (" + (due==0?"✓":due) + ")");

            if (due > 0 && !foundStartableTest)
                foundStartableTest = true;
        }

        // Show/hide study status text
        TextView dueTextView = (TextView)findViewById(R.id.tests_due_text);
        TextView doneTextView = (TextView)findViewById(R.id.tests_done_text);
        if (foundStartableTest) {
            dueTextView.setVisibility(View.VISIBLE);
            doneTextView.setVisibility(View.GONE);
        } else {
            dueTextView.setVisibility(View.GONE);
            doneTextView.setVisibility(View.VISIBLE);
        }

        // Clear any word lists from previous active tests
        WordInformationTest.wordInformationIdsList = null;
        WordInformationTestActivity.currentWordInformation = null;

        // Stop any TTS utterances from tests just finished
        MainActivity.ttsStop();

        if (MainActivity.isInTutorial()) {
            // Has user done a quad test?
            if (KiokuServerClient.getPreferences(this).getBoolean("quadHintSeen", false)) {
                firstHint.setVisibility(View.GONE);
                secondHint.setVisibility(View.VISIBLE);
            } else {
                firstHint.setVisibility(View.VISIBLE);
                secondHint.setVisibility(View.GONE);
            }
        }
    }
}
