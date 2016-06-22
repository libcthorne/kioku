package me.cthorne.kioku;

/**
 * Created by chris on 09/03/16.
 */

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;

import me.cthorne.kioku.helpers.Checks;
import me.cthorne.kioku.helpers.CleanBeforeIntentsTestRule;
import me.cthorne.kioku.languages.AddLanguagesActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.times;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by chris on 09/03/16.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddNoLanguages {

    @Rule
    public CleanBeforeIntentsTestRule<AddLanguagesActivity> mActivityRule = new CleanBeforeIntentsTestRule<>(AddLanguagesActivity.class);

    @Test
    public void addLanguage() throws SQLException, InterruptedException {

        // Click save button without selecting anything
        onView(withId(R.id.add_languages_button)).perform(click());

        // Check for error
        Checks.checkToastMessageDisplayed(mActivityRule.getActivity(), R.string.add_languages_none_selected_error);

        // Check activity is not changed
        intended(hasComponent(MainActivity.class.getName()), times(0));

    }



}

