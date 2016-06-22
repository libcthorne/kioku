package me.cthorne.kioku;

/**
 * Created by chris on 09/03/16.
 */

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;

import me.cthorne.kioku.helpers.CleanBeforeIntentsTestRule;
import me.cthorne.kioku.languages.AddLanguagesActivity;
import me.cthorne.kioku.languages.SelectedWordLanguage;
import me.cthorne.kioku.words.WordLanguage;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

/**
 * Created by chris on 09/03/16.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddLanguageEN {

    @Rule
    public CleanBeforeIntentsTestRule<AddLanguagesActivity> mActivityRule = new CleanBeforeIntentsTestRule<>(AddLanguagesActivity.class);

    @Test
    public void addLanguage() throws SQLException, InterruptedException {

        // Select English
        onData(hasToString(equalTo("English")))
                .inAdapterView(withId(R.id.languages_listview))
                .perform(click());

        // Click save button
        onView(withId(R.id.add_languages_button)).perform(click());

        // Check language was saved in database
        DatabaseHelper dbHelper = OpenHelperManager.getHelper(getTargetContext(), DatabaseHelper.class);
        assertTrue(dbHelper.getUserLanguages().size() == 1);
        assertTrue(dbHelper.getUserLanguages().contains(new SelectedWordLanguage(getTargetContext(), WordLanguage.EN)));
        OpenHelperManager.releaseHelper();

        // Check the new activity
        intended(hasComponent(MainActivity.class.getName()));

        // Check the language is in the spinner

        // 1. click on spinner
        onView(withId(R.id.language_spinner)).perform(click());

        // 2. select Japanese
        onData(hasToString("English"))
                //.inAdapterView(withId(R.id.language_spinner))
                .perform(click());

        // 3. check it was selected
        onView(withId(R.id.language_spinner))
                .check(matches(withSpinnerText(containsString("English"))));
    }

}

