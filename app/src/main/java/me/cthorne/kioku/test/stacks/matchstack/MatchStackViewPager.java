package me.cthorne.kioku.test.stacks.matchstack;

import android.content.Context;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.MotionEvent;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.test.stacks.StackViewPager;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 12/01/16.
 */
public class MatchStackViewPager extends StackViewPager {
    private static final int BOTTOM_OFFSCREEN_PAGES = 4;
    private static final long CORRECTED_ANSWER_DISPLAY_TIME = 1000;

    private boolean pendingTransition;

    public MatchStackViewPager(Context context) {
        super(context);
    }

    public MatchStackViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void checkAnswer() {
        // Ignore answers when showing the correct answer after an incorrect answer
        if (pendingTransition)
            return;

        final MatchStackFragmentAdapter adapter = (MatchStackFragmentAdapter)getAdapter();

        MatchStackFragment topFragment = (MatchStackFragment)adapter.getSavedFragment(getCurrentItem());

        if (topFragment.getBottomViewPager().isAnswerCorrect()) {
            adapter.processCorrectAnswer(topFragment.getWordInformation());

            // Check if we've finished
            if (getCurrentItem() + 1 == adapter.getCount()) {
                adapter.markAsFinished();
            }

            // Transition to next question
            setCurrentItem(getCurrentItem() + 1);
        } else {
            adapter.processIncorrectAnswer(topFragment.getWordInformation());

            // Show correct answer
            topFragment.getBottomViewPager().setToCorrectItem();

            // Transition to next question after 1 second
            pendingTransition = true;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Check if we've finished
                    if (getCurrentItem() + 1 == adapter.getCount()) {
                        adapter.markAsFinished();
                    }

                    // Transition to next question
                    setCurrentItem(getCurrentItem() + 1);

                    pendingTransition = false;
                }
            }, CORRECTED_ANSWER_DISPLAY_TIME);
        }
    }

    public MatchStackItemFragmentAdapter fillBottom(MatchStackItemViewPager bottomViewPager, DatabaseHelper dbHelper, WordInformation wordInformation) {
        MatchStackFragmentAdapter thisAdapter = (MatchStackFragmentAdapter)getAdapter();

        AppCompatActivity activity = (AppCompatActivity)thisAdapter.getContext();
        MatchStackItemFragmentAdapter adapter = new MatchStackItemFragmentAdapter(dbHelper, activity.getSupportFragmentManager(), wordInformation, thisAdapter.getStackHandler());

        bottomViewPager.setAdapter(adapter);
        bottomViewPager.setOffscreenPageLimit(BOTTOM_OFFSCREEN_PAGES);

        return adapter;
    }

    /* Source: http://stackoverflow.com/a/9650884/5402565 */

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return false;
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item);

        ((MatchStackFragmentAdapter)getAdapter()).onShowFragment(item);
    }
}
