package me.cthorne.kioku.helpers;

import android.app.Activity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Created by chris on 10/03/16.
 */
public class Checks {
    public static void checkToastMessageDisplayed(Activity activity, String text) {
        onView(withText(text))
                .inRoot(withDecorView(not(is(activity.getWindow().getDecorView()))))
                .check(matches(isDisplayed()));
    }

    public static void checkToastMessageDisplayed(Activity activity, int textId) {
        checkToastMessageDisplayed(activity, activity.getString(textId));
    }

    public static void checkToastMessageNotDisplayed(Activity activity, String text) {
        onView(withText(text))
                .inRoot(withDecorView(not(is(activity.getWindow().getDecorView()))))
                .check(doesNotExist());
    }

    public static void checkToastMessageNotDisplayed(Activity activity, int textId) {
        checkToastMessageNotDisplayed(activity, activity.getString(textId));
    }
}
