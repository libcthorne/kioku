package me.cthorne.kioku;

/**
 * Created by chris on 09/03/16.
 */

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;
import java.util.Date;

import me.cthorne.kioku.auth.UserAccount;
import me.cthorne.kioku.helpers.CleanBeforeIntentsTestRule;
import me.cthorne.kioku.languages.SelectedWordLanguage;
import me.cthorne.kioku.words.Word;
import me.cthorne.kioku.words.WordInformation;
import me.cthorne.kioku.words.WordInformationType;
import me.cthorne.kioku.words.WordLanguage;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by chris on 09/03/16.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class Study {

    @Rule
    public CleanBeforeIntentsTestRule<MainActivity> mActivityRule = new CleanBeforeIntentsTestRule<>(MainActivity.class, new CleanBeforeIntentsTestRule.PostCleanCallback() {
        @Override
        public void run(Context context) {
            DatabaseHelper dbHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

            try {
                // Add Japanese language
                SelectedWordLanguage jp = new SelectedWordLanguage(context, WordLanguage.JP);
                dbHelper.getSelectedWordLanguageDao().create(jp);

                // Add Japanese word
                Word word = new Word();
                word.setLanguage(jp.getLanguage().getValue());
                word.setUserAccount(new UserAccount(KiokuServerClient.getCurrentUserId(context)));
                word.setCreatedAt(new Date());
                dbHelper.getWordDao().create(word);

                // Add word information
                WordInformation info1 = new WordInformation();
                info1.setInformationType(WordInformationType.WORD_FORM);
                info1.setInformationBytes("犬".getBytes());
                info1.setMetaInformationBytes("いぬ".getBytes());
                info1.setWord(word);
                dbHelper.getWordInformationDao().create(info1);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            OpenHelperManager.releaseHelper();
        }
    });

    @Test
    public void study() throws InterruptedException {
        // Press study button
        onView(withId(R.id.study_button)).perform(scrollTo(), click());

        // Check activity is changed
        intended(hasComponent(StudyActivity.class.getName()));

        // Comprehension test

    }

}

