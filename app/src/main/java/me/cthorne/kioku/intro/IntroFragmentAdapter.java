package me.cthorne.kioku.intro;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import me.cthorne.kioku.R;

/**
 * Created by chris on 21/02/16.
 */
public class IntroFragmentAdapter extends FragmentPagerAdapter {
    public IntroFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        int layout;

        switch (position) {
            case 0:
                layout = R.layout.activity_intro_screen1;
                break;
            case 1:
                layout = R.layout.activity_intro_screen_step1;
                break;
            case 2:
                layout = R.layout.activity_intro_screen_step2;
                break;
            case 3:
                layout = R.layout.activity_intro_screen_step3;
                break;
            case 4:
                layout = R.layout.activity_intro_screen_step4;
                break;
            case 5:
                layout = R.layout.activity_intro_screen5;
                break;
            default:
                return null;
        }

        return IntroFragment.newInstance(layout);
    }

    @Override
    public int getCount() {
        return 6;
    }
}
