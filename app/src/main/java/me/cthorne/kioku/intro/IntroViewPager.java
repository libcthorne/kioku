package me.cthorne.kioku.intro;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
 * Created by chris on 21/02/16.
 */
public class IntroViewPager extends ViewPager {

    private Context context;

    public IntroViewPager(Context context) {
        super(context);
        init(context);
    }

    public IntroViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        this.context = context;
    }



}
