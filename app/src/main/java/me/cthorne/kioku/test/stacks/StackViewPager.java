package me.cthorne.kioku.test.stacks;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
 * Created by chris on 09/02/16.
 */
public class StackViewPager extends ViewPager {

    public StackViewPager(Context context) {
        super(context);
    }

    public StackViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item);

        ((StackFragmentAdapter)getAdapter()).onShowFragment(item);
    }

}
