package me.cthorne.kioku.test.stacks.quadstack;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import me.cthorne.kioku.test.WordInformationTestActivity;

/**
 * Created by chris on 19/02/16.
 */
public class QuadStackCardViewPager extends ViewPager {

    private Context context;

    private QuadStackViewPager stackViewPager;

    private float touchStartX;
    private float touchStartY;
    private float touchEndX;
    private float touchEndY;

    private final static float SWIPE_THRESHOLD = 20.0f;

    public final static int TAG_QUESTION = 0;
    public final static int TAG_ANSWER = 1;
    public final static int TAG_LOADING = 2;

    public QuadStackCardViewPager(Context context) {
        super(context);
        init(context);
    }

    public QuadStackCardViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        this.context = context;
        setPageTransformer(true, new QuadStackCardPageTransformer());

        stackViewPager = (QuadStackViewPager)((WordInformationTestActivity) getContext()).findViewById(QuadStackTest.stackViewPagerId);
    }

    private class QuadStackCardPageTransformer implements ViewPager.PageTransformer {
        @Override
        public void transformPage(View view, float position) {
            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
            } else if (position <= 1) { // [-1,1]
                // Counteract the default slide transition for answer (i.e. stop it moving)
                if (view.getTag() == TAG_ANSWER)
                    view.setTranslationX(view.getWidth() * -position);
            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
            }
        }
    }

    // The back of the card was shown by this press in ACTION_DOWN
    private boolean showBackPress;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);

        QuadStackFragment currentFragment = stackViewPager.getCurrentFragment();
        if (currentFragment.isLoadingFragment)
            return false; // ignore swipes while loading

        float xDiff, yDiff;

        QuadStackCardFragment backFragment = ((QuadStackCardFragmentAdapter)stackViewPager.getCurrentFragment().cardViewPager.getAdapter()).backFragment;
        RelativeLayout upButton = backFragment.upButton;
        RelativeLayout downButton = backFragment.downButton;

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                touchStartX = event.getRawX();
                touchStartY = event.getRawY();

                if (!stackViewPager.isShowingBack()) {
                    Log.d("kioku-touch", "action_down showBack");
                    stackViewPager.showBack();
                    showBackPress = true;
                }

                return true;
            case (MotionEvent.ACTION_MOVE):
                touchEndX = event.getRawX();
                touchEndY = event.getRawY();

                //Log.d("kioku-touch", touchStartX +","+ touchStartY +","+ touchEndX +","+ touchEndY);

                xDiff = touchEndX - touchStartX;
                yDiff = touchEndY - touchStartY;

                if (Math.abs(xDiff) > Math.abs(yDiff)) { // horizontal swipe
                    if (xDiff > SWIPE_THRESHOLD)
                        ;//stackViewPager.showFront();

                    if (xDiff < -SWIPE_THRESHOLD)
                        ;//stackViewPager.showBack();

                    upButton.setAlpha(1);
                    upButton.setScaleX(1);
                    upButton.setScaleY(1);
                    downButton.setAlpha(1);
                    downButton.setScaleX(1);
                    downButton.setScaleY(1);
                } else { // vertical swipe
                    float swipedPercent = Math.abs(yDiff)/SWIPE_THRESHOLD;

                    //Log.d("kioku-touch", "sP: " + swipedPercent);

                    if (Math.abs(yDiff) > SWIPE_THRESHOLD) {
                        boolean correct = yDiff <= 0;

                        float growPercent = Math.min(1, (Math.abs(yDiff)-SWIPE_THRESHOLD)/400); // TODO: 400 -> screen size?

                        if (correct) {
                            upButton.setScaleX(1+growPercent);
                            upButton.setScaleY(1+growPercent);
                            downButton.setScaleX(1-growPercent);
                            downButton.setScaleY(1-growPercent);
                        } else {
                            upButton.setScaleX(1-growPercent);
                            upButton.setScaleY(1-growPercent);
                            downButton.setScaleX(1+growPercent);
                            downButton.setScaleY(1+growPercent);
                        }
                    }
                }

                return super.onTouchEvent(event);
            case (MotionEvent.ACTION_UP):
                upButton.setAlpha(1);
                upButton.setScaleX(1);
                upButton.setScaleY(1);
                downButton.setAlpha(1);
                downButton.setScaleX(1);
                downButton.setScaleY(1);


                touchEndX = event.getRawX();
                touchEndY = event.getRawY();

                //Log.d("kioku-touch", touchStartX +","+ touchStartY +","+ touchEndX +","+ touchEndY);

                xDiff = touchEndX - touchStartX;
                yDiff = touchEndY - touchStartY;

                //if (stackViewPager.isShowingBack()) {
                    if (Math.abs(xDiff) >= Math.abs(yDiff)) { // horizontal swipe or touch

                        if (!showBackPress)
                            stackViewPager.flipCard();

                        //if (xDiff > SWIPE_THRESHOLD)
                        //    stackViewPager.showFront();
                        //if (xDiff < -SWIPE_THRESHOLD)
                        //    stackViewPager.showBack();
                    } else if (Math.abs(yDiff) > SWIPE_THRESHOLD) { // vertical swipe
                        boolean correct = yDiff <= 0;
                        // Vertical transition to next question
                        stackViewPager.showNext(correct);
                    } else { // neither direction; treat as a press
                        if (!showBackPress)
                            stackViewPager.flipCard();
                    }
                //} else {

                //}

                showBackPress = false;

                return super.onTouchEvent(event);
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //return true; // Make sure we get the touch events, not the children
        return false; // Forward events to children
    }

}
