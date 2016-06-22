package me.cthorne.kioku.helpers;

import android.app.Activity;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;

import java.util.Collection;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.runner.lifecycle.Stage.RESUMED;

/**
 * Created by chris on 19/03/16.
 */
public class ActivityHelpers {
    private static Activity currentActivity;

    // http://qathread.blogspot.co.uk/2014/09/discovering-espresso-for-android-how-to.html
    public static Activity getCurrentActivity(){
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(RESUMED);
                if (resumedActivities.iterator().hasNext()){
                    currentActivity = (Activity)resumedActivities.iterator().next();
                }
            }
        });

        return currentActivity;
    }
}
