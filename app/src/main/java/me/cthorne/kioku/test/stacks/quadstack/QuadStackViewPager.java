package me.cthorne.kioku.test.stacks.quadstack;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import me.cthorne.kioku.KiokuServerClient;
import me.cthorne.kioku.test.stacks.StackViewPager;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 17/11/15.
 * Referenced:
 * - http://stackoverflow.com/a/22797619/5402565
 * - http://developer.android.com/training/gestures/detector.html
 * - http://developer.android.com/training/animation/screen-slide.html
 */
public class QuadStackViewPager extends StackViewPager {

    private Context context;

    private float verticalMultiplier;

    public final static float SWIPE_THRESHOLD = 20.0f;

    public final static int TAG_QUESTION = 0;
    public final static int TAG_ANSWER = 1;
    public final static int TAG_LOADING = 2;

    private QuadStackCardFragmentAdapter cardAdapter;

    public QuadStackViewPager(Context context) {
        super(context);
        init(context);
    }

    public QuadStackViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        this.context = context;
        setPageTransformer(true, new QuadStackPageTransformer());
    }

    public QuadStackCardFragmentAdapter fillCard(QuadStackCardViewPager cardViewPager, WordInformation wordInformation, boolean first) {
        QuadStackFragmentAdapter thisAdapter = (QuadStackFragmentAdapter)getAdapter();

        AppCompatActivity activity = (AppCompatActivity)thisAdapter.getContext();

        cardAdapter = new QuadStackCardFragmentAdapter(activity.getSupportFragmentManager(), wordInformation, thisAdapter.getStackHandler(), first);

        cardViewPager.setAdapter(cardAdapter);
        cardViewPager.setPageMargin(0);

        return cardAdapter;
    }

    public void flipCard() {
        if (isShowingBack())
            showFront();
        else
            showBack();
    }

    private class QuadStackPageTransformer implements ViewPager.PageTransformer {
        @Override
        public void transformPage(View view, float position) {
            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the top.
            } else if (position <= 1) { // [-1,1]
                // Counteract the default slide transition
                view.setTranslationX(view.getWidth() * -position);

                // Vertical transition
                view.setTranslationY(view.getHeight() * -position * verticalMultiplier);
            } else { // (1,+Infinity]
                // This page is way off-screen to the bottom.
            }
        }
    }

    /**
     * Shows the next card in the stack.
     * @param correct whether the answer to the current card was a "got it" or "forgot"
     */
    public void showNext(boolean correct) {
        if (!isShowingBack())
            return;

        Log.d("kioku-quad", "showNext");

        QuadStackFragmentAdapter adapter = (QuadStackFragmentAdapter)getAdapter();
        QuadStackFragment fragment = getCurrentFragment();

        // Process answer
        if (correct)
            adapter.processCorrectAnswer(fragment.getWordInformation());
        else
            adapter.processIncorrectAnswer(fragment.getWordInformation());

        // Vertical transition for next fragment
        // Direction depends on answer
        verticalMultiplier = correct ? -1 : 1;

        // Check if finished
        if (getCurrentItem()+1 == adapter.getCount())
            adapter.markAsFinished();

        // Hide current fragment
        if (fragment.getView() != null)
            fragment.getView().setVisibility(GONE);

        // Go to next fragment with vertical transition
        setCurrentItem(getCurrentItem()+1);

        if (!KiokuServerClient.getPreferences(getContext()).getBoolean("quadHintSeen", false))
            KiokuServerClient.getPreferences(getContext()).edit().putBoolean("quadHintSeen", true).commit();
    }

    public QuadStackFragment getCurrentFragment() {
        QuadStackFragmentAdapter adapter = (QuadStackFragmentAdapter)getAdapter();
        QuadStackFragment currentFragment = (QuadStackFragment)adapter.getSavedFragment(getCurrentItem());
        return currentFragment;
    }

    /**
     * Shows the front of the current card.
     */
    public void showFront() {
        Log.d("kioku-quad", "showFront");

        QuadStackFragment currentFragment = getCurrentFragment();
        currentFragment.cardViewPager.setCurrentItem(0);
    }

    /**
     * Shows the back of the current card.
     */
    public void showBack() {
        Log.d("kioku-quad", "showBack");

        QuadStackFragment currentFragment = getCurrentFragment();
        currentFragment.cardViewPager.setCurrentItem(1);
    }

    public boolean isShowingBack() {
        QuadStackFragment currentFragment = getCurrentFragment();

        if (currentFragment.isLoadingFragment)
            return false;

        return currentFragment.cardViewPager.getCurrentItem() == 1;
    }

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
}
