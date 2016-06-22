package me.cthorne.kioku;

/**
 * Created by chris on 09/03/16.
 */

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
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by chris on 09/03/16.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SkipTutorial {

    @Rule
    public IntentsTestRule<IntroActivity> mActivityRule = new IntentsTestRule<>(IntroActivity.class);

    @Before
    public void clear() {
        KiokuServerClient.getPreferences(getTargetContext()).edit().clear().commit();
    }

    @Test
    public void skipIntro() {

        // Press skip button
        onView(withId(R.id.skip_button)).perform(click());

        // Check the new activity
        intended(hasComponent(LoginActivity.class.getName()));

    }

}

