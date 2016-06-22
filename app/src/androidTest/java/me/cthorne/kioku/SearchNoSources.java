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

import me.cthorne.kioku.helpers.Checks;
import me.cthorne.kioku.helpers.CleanBeforeIntentsTestRule;
import me.cthorne.kioku.infosources.AddSourcesActivity;
import me.cthorne.kioku.languages.SelectedWordLanguage;
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
public class SearchNoSources {

    @Rule
    public CleanBeforeIntentsTestRule<MainActivity> mActivityRule = new CleanBeforeIntentsTestRule<>(MainActivity.class, new CleanBeforeIntentsTestRule.PostCleanCallback() {
        @Override
        public void run(Context context) {
            // Add Japanese language
            DatabaseHelper dbHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
            SelectedWordLanguage jp = new SelectedWordLanguage(context, WordLanguage.JP);
            try {
                dbHelper.getSelectedWordLanguageDao().create(jp);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            OpenHelperManager.releaseHelper();

            // Not in tutorial
            KiokuServerClient.getPreferences(context).edit().putBoolean("inTutorial", false).commit();
        }
    });

    @Test
    public void search() {
        // Press search button
        onView(withId(R.id.search_button)).perform(scrollTo(), click());

        // Check activity is changed
        intended(hasComponent(SearchActivity.class.getName()));
        intended(hasComponent(AddSourcesActivity.class.getName()));

        // Check for error
        Checks.checkToastMessageDisplayed(mActivityRule.getActivity(), R.string.search_no_sources_error);
    }

}

