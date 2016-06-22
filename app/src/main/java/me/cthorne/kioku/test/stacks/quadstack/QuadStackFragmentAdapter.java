package me.cthorne.kioku.test.stacks.quadstack;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import java.util.List;

import me.cthorne.kioku.test.WordInformationTest;
import me.cthorne.kioku.test.stacks.StackFragmentAdapter;
import me.cthorne.kioku.test.tests.Tests;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 23/11/15.
 */
public class QuadStackFragmentAdapter extends StackFragmentAdapter {
    public static QuadStackHandler stackHandler;
    private QuadStackTest test;

    public QuadStackFragmentAdapter(Context context, FragmentManager fm, List<Integer> wordInformationIdsList, QuadStackHandler stackHandler, QuadStackTest test) {
        super(context, fm, wordInformationIdsList, test);

        this.stackHandler = stackHandler;
        this.test = test;
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
            return QuadStackFragment.newInstance(null, false, false);
        } else {
            boolean current = (position == currentFragment);
            Integer wordInformationId = wordInformationIdsList.get(position);

            return QuadStackFragment.newInstance(wordInformationId, current, position == 0);
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

    public QuadStackHandler getStackHandler() {
        return stackHandler;
    }

}
