package me.cthorne.kioku;

import android.support.test.espresso.intent.rule.IntentsTestRule;
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
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by chris on 10/03/16.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class RegistrationInvalidEmail {

    @Rule
    public IntentsTestRule<RegisterActivity> mActivityRule = new IntentsTestRule<>(RegisterActivity.class);

    @Before
    public void clear() {
        KiokuServerClient.getPreferences(getTargetContext()).edit().clear().commit();
    }

    @Test
    public void register() {
        // Registration details
        // Invalid email + valid password
        onView(withId(R.id.email_text)).perform(typeText("test.com"), closeSoftKeyboard());
        onView(withId(R.id.password_text)).perform(typeText("testtest"), closeSoftKeyboard());
        onView(withId(R.id.password_confirmation_text)).perform(typeText("testtest"), closeSoftKeyboard());

        // Press register button
        onView(withId(R.id.register_button)).perform(click());

        // Check for error
        Checks.checkToastMessageDisplayed(mActivityRule.getActivity(), R.string.registration_invalid_email_error);
    }

}