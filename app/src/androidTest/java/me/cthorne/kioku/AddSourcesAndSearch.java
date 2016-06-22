package me.cthorne.kioku;

import android.content.Context;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.web.webdriver.Locator;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.sql.SQLException;

import me.cthorne.kioku.helpers.CleanBeforeIntentsTestRule;
import me.cthorne.kioku.infosources.AddSourcesActivity;
import me.cthorne.kioku.languages.SelectedWordLanguage;
import me.cthorne.kioku.words.Word;
import me.cthorne.kioku.words.WordLanguage;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static android.support.test.espresso.web.webdriver.DriverAtoms.findElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webClick;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasToString;

/**
 * Created by chris on 12/03/16.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AddSourcesAndSearch {

    private static final int MAX_PAGE_WAIT_SECONDS = 10;

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
        }
    });

    @Test
    public void addSources() throws SQLException, InterruptedException {
        // Press search button
        onView(withId(R.id.search_button)).perform(scrollTo(), click());

        // Check activity is changed
        intended(hasComponent(SearchActivity.class.getName()));
        intended(hasComponent(AddSourcesActivity.class.getName()));

        // Wait for info to be retrieved
        // TODO: replace with async check?
        Thread.sleep(1000);

        // Check Jisho.org Dictionary is selected
        onData(hasToString(containsString("Jisho.org Dictionary")))
                .inAdapterView(withId(R.id.sources_listview))
                .onChildView(withId(R.id.row_checkbox))
                .check(matches(isChecked()));
                //.perform(click());

        // Check Google Images is selected
        onData(hasToString(containsString("Google Images")))
                .inAdapterView(withId(R.id.sources_listview))
                .onChildView(withId(R.id.row_checkbox))
                .check(matches(isChecked()));
        //.perform(click());

        // Click save button to get default sources
        // Defaults: Jisho.org Dictionary, Google Images (JP)
        onView(withId(R.id.add_sources_button)).perform(click());

        // Check the new activity
        intended(hasComponent(SearchActivity.class.getName()));

/*
        // Press edit sources button
        onView(withId(R.id.information_sources_settings)).perform(click());

        // Check sources were saved in database
        // Try 5 times with 1 second interval
        boolean sourcesFound = false;
        for (int i = 0; i < 3; i++) {
            //DatabaseHelper dbHelper = ((MainActivity)mActivityRule.getActivity()).getHelper();
            DatabaseHelper dbHelper = ((SourcesActivity) ActivityHelpers.getCurrentActivity()).getHelper();

            List<SelectedWordInformationSource> selectedSources = dbHelper.qbSelectedUserSources(true, WordLanguage.JP).query();
            assertTrue(selectedSources.size() == 2);
            Log.d("kioku-test", "0: " + selectedSources.get(0).getSource().getTitle());
            Log.d("kioku-test", "1: " + selectedSources.get(1).getSource().getTitle());

            sourcesFound = selectedSources.get(0).getSource().getTitle().contains("Jisho.org Dictionary")
                            && selectedSources.get(1).getSource().getTitle().contains("Google Images");

            if (sourcesFound)
                break;

            // Wait for downloads to finish
            // TODO: replace with async check?
            Thread.sleep(1000);
        }

        assertTrue(sourcesFound);
*/

        // Search query
        onView(withId(R.id.main_edittext)).perform(typeText("dog"));
        // Press search button (on search screen)
        onView(withId(R.id.search_button)).perform(click());

        // Check the new activity
        intended(hasComponent(SearchResultsActivity.class.getName()));

        // Wait for page to load
        for (int i = 0; i < MAX_PAGE_WAIT_SECONDS; i++) {
            Thread.sleep(1000);

            try {
                // Click select button
                onView(allOf(withId(R.id.search_select_button), isDisplayed(), isClickable()))
                        .perform(click());

                break;
            } catch (NoMatchingViewException e) {
                if (i == MAX_PAGE_WAIT_SECONDS-1)
                    throw e;
            }
        }

        // Select first selectable in first web view (the word)
        onWebView(allOf(withId(R.id.fragment_web_view), hasFocus())).withElement(findElement(Locator.CSS_SELECTOR, "._kioku_selectable:nth-of-type(1)")).perform(webClick());

        Thread.sleep(1000);

        // Save information
        onView(withId(R.id.save_word_button)).perform(click());

        // Finish
        onView(withId(R.id.save_word_button)).perform(click());

        // Check word was saved
        DatabaseHelper dbHelper = ((MainActivity)mActivityRule.getActivity()).getHelper();
        assertTrue(dbHelper.getWordDao().countOf() == 1);

        Word savedWord = dbHelper.getWordDao().queryForAll().get(0);
        WordForm wordForm = savedWord.getForms(dbHelper).get(0);
        assertTrue(wordForm.getForm().equals("犬"));
        assertTrue(wordForm.getFormMeta().equals("いぬ"));
    }
}
