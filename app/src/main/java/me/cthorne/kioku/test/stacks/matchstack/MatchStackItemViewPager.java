package me.cthorne.kioku.test.stacks.matchstack;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by chris on 12/01/16.
 */
public class MatchStackItemViewPager extends ViewPager {
    public MatchStackItemViewPager(Context context) {
        super(context);
    }

    public MatchStackItemViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isAnswerCorrect() {
        Log.d("kioku-tmp", getCurrentItem() + "," + ((MatchStackItemFragmentAdapter) getAdapter()).getCorrectAnswerPosition());
        return getCurrentItem() == ((MatchStackItemFragmentAdapter)getAdapter()).getCorrectAnswerPosition();
    }

    /**
     * Set current item to correct item.
     */
    public void setToCorrectItem() {
        setCurrentItem(((MatchStackItemFragmentAdapter)getAdapter()).getCorrectAnswerPosition(), true);
    }
}
