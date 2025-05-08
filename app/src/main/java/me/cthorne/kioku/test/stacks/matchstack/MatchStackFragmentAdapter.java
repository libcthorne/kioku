package me.cthorne.kioku.test.stacks.matchstack;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.util.Log;

import java.util.List;

import me.cthorne.kioku.test.WordInformationTest;
import me.cthorne.kioku.test.stacks.StackFragmentAdapter;
import me.cthorne.kioku.test.tests.Tests;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 12/01/16.
 */
public class MatchStackFragmentAdapter extends StackFragmentAdapter {
    public static MatchStackHandler stackHandler;

    public MatchStackFragmentAdapter(Context context, FragmentManager fm, List<Integer> wordInformationIdsList, MatchStackHandler stackHandler, MatchStackTest test) {
        super(context, fm, wordInformationIdsList, test);

        MatchStackFragmentAdapter.stackHandler = stackHandler;
    }

    @Override
    public int getCount() {
        // Special case for finished fragment
        if (finished)
            return wordInformationIdsList.size()+1;

        return wordInformationIdsList.size();
    }

    @Override
    public Fragment getItem(int position) {
        if (finished) {
            return MatchStackFragment.newInstance(null, false, false);
        } else {
            Integer wordInformationId = wordInformationIdsList.get(position);
            boolean current = (position == currentFragment);

            return MatchStackFragment.newInstance(wordInformationId, current, position == 0);
        }
    }

    /**
     * User answered question correctly for word information.
     */
    @Override
    public void processCorrectAnswer(WordInformation wordInformation) {
        Log.d("kioku-test", "Correct for word " + wordInformation.id);

        test.processCorrectAnswer(wordInformation, getAndResetAnswerTime());
    }

    /**
     * User answered question incorrectly for word information.
     */
    @Override
    public void processIncorrectAnswer(WordInformation wordInformation) {
        Log.d("kioku-test", "Incorrect for word " + wordInformation.id);

        test.processIncorrectAnswer(wordInformation, getAndResetAnswerTime());

        if (!Tests.hasEasierTestThanCurrent(WordInformationTest.activity.getHelper())) {
            // Add word information to the end of the word information list to be seen again.
            wordInformationIdsList.add(wordInformation.id);
            notifyDataSetChanged();
        }
    }

    public MatchStackHandler getStackHandler() {
        return stackHandler;
    }

}
