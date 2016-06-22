package me.cthorne.kioku;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import me.cthorne.kioku.helpers.Checks;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by chris on 09/03/16.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginNoPassword {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(LoginActivity.class);

    @Before
    public void clear() {
        KiokuServerClient.getPreferences(getTargetContext()).edit().clear().commit();
    }

    @Test
    public void login() {
        // Type text and then press the button
        onView(withId(R.id.email_text)).perform(typeText("username"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(scrollTo(), click());

        // Check that the text was changed
        onView(withId(R.id.email_text)).check(matches(withText("username")));

        // Check for error
        Checks.checkToastMessageDisplayed(mActivityRule.getActivity(), R.string.login_auth_error);
    }

}
