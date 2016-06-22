package me.cthorne.kioku;

import android.content.ComponentName;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by chris on 10/03/16.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class RegistrationFromLogin {

    @Rule
    public IntentsTestRule<LoginActivity> mActivityRule = new IntentsTestRule<>(LoginActivity.class);

    @Before
    public void clear() {
        KiokuServerClient.getPreferences(getTargetContext()).edit().clear().commit();
    }

    @Test
    public void register() {
        // Registration details on login screen
        onView(withId(R.id.email_text)).perform(typeText("test@test.com"), closeSoftKeyboard());
        onView(withId(R.id.password_text)).perform(typeText("testtest"), closeSoftKeyboard());

        // Press register button
        onView(withId(R.id.register_button)).perform(click());

        // Check the result is the registration screen
        intended(hasComponent(new ComponentName(getTargetContext(), RegisterActivity.class)));

        // Check details are copied correctly
        onView(withId(R.id.email_text)).check(matches(withText("test@test.com")));
        onView(withId(R.id.password_text)).check(matches(withText("testtest")));
        onView(withId(R.id.password_confirmation_text)).check(matches(withText("")));
    }

}