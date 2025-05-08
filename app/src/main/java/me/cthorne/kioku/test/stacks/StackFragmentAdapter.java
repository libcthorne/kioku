package me.cthorne.kioku.test.stacks;

import android.content.Context;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.cthorne.kioku.test.WordInformationTest;
import me.cthorne.kioku.test.tests.Tests;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 12/01/16.
 */
public abstract class StackFragmentAdapter extends FragmentPagerAdapter {

    private static final float MAX_ANSWER_TIME_SECONDS = 30.0f;
    private static final int DEFAULT_LOADING_TRANSITION_TIME = 500;
    private static final int DEFAULT_FINISHED_TRANSITION_TIME = 1000;

    protected Context context;
    protected List<Integer> wordInformationIdsList;
    protected WordInformationTest test;
    protected boolean finished;
    protected Map<Integer, Fragment> savedFragments = new HashMap<>();

    protected DateTime answerStartTime;

    protected int currentFragment = 0;

    public StackFragmentAdapter(Context context, FragmentManager fm, List<Integer> wordInformationIdsList, WordInformationTest test) {
        super(fm);

        this.context = context;
        this.wordInformationIdsList = wordInformationIdsList;
        this.answerStartTime = new DateTime();
        this.test = test;
    }

    /**
     * Gets time taken to answer in seconds.
     * Clamped at 30 seconds.
     * @return
     */
    public float getAnswerTime() {
        DateTime now = new DateTime();

        return Math.min(MAX_ANSWER_TIME_SECONDS, (now.getMillis()-answerStartTime.getMillis())/1000.0f);
    }

    public void resetAnswerTime() {
        answerStartTime = new DateTime();
    }

    public float getAndResetAnswerTime() {
        float t = getAnswerTime();
        resetAnswerTime();
        return t;
    }

    public Fragment getSavedFragment(int position) {
        return savedFragments.get(position);
    }

    // Adapted from http://stackoverflow.com/questions/14035090/how-to-get-existing-fragments-when-using-fragmentpageradapter
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment)super.instantiateItem(container, position);

        savedFragments.put(position, createdFragment);

        return createdFragment;
    }

    public void markAsFinished() {
        markAsFinished(Tests.hasEasierTestThanCurrent(WordInformationTest.activity.getHelper()) ?
                DEFAULT_LOADING_TRANSITION_TIME :
                DEFAULT_FINISHED_TRANSITION_TIME);
    }

    public void markAsFinished(int delayMs) {
        finished = true;
        notifyDataSetChanged();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                test.finishTest();
            }
        }, delayMs);
    }

    public List<Integer> getWordInformationIdsList() {
        return wordInformationIdsList;
    }

    public Context getContext() {
        return context;
    }

    public abstract void processCorrectAnswer(WordInformation wordInformation);
    public abstract void processIncorrectAnswer(WordInformation wordInformation);

    public Map<Integer, Fragment> getSavedFragments() {
        return savedFragments;
    }

    public void onShowFragment(int item) {
        Log.d("kioku-stack", "onShowFragment(" + item + ")");

        if (finished)
            return;

        currentFragment = item;

        if (savedFragments.size() > item) // if the fragment is created already, trigger show callback here
            ((StackFragment)getSavedFragment(item)).onFragmentShow();
    }

    public boolean isFinished() {
        return finished;
    }
}
