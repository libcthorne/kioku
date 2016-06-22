package me.cthorne.kioku.test.stacks;

import android.support.v4.app.Fragment;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.cthorne.kioku.test.WordInformationTest;
import me.cthorne.kioku.test.WordInformationTestActivity;
import me.cthorne.kioku.test.tests.Tests;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 08/02/16.
 */
public abstract class StackFragment extends Fragment {

    protected WordInformation wordInformation;

    protected void showLoadingFragment(ViewGroup v) {
        v.removeAllViews();

        RelativeLayout container = new RelativeLayout(getContext());
        RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        if (Tests.hasTestAfterCurrent(WordInformationTest.activity.getHelper())) {
            // Loading for next test

            ProgressBar progress = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleSmall);

            RelativeLayout.LayoutParams progressParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            progressParams.addRule(RelativeLayout.CENTER_IN_PARENT);

            container.addView(progress, progressParams);
        } else {
            // No more tests, show "well done" message

            TextView wellDone = new TextView(getContext());
            wellDone.setText("Well done!");

            RelativeLayout.LayoutParams wellDoneParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            wellDoneParams.addRule(RelativeLayout.CENTER_IN_PARENT);

            container.addView(wellDone, wellDoneParams);
        }

        v.addView(container, containerParams);
    }

    public void onFragmentShow() {
        WordInformationTestActivity.currentWordInformation = wordInformation;
    }

}
