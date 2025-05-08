package me.cthorne.kioku.test.stacks.quadstack;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 19/02/16.
 */
public class QuadStackCardFragmentAdapter extends FragmentPagerAdapter {

    private WordInformation wordInformation;
    private QuadStackHandler stackHandler;

    public QuadStackCardFragment frontFragment;
    public QuadStackCardFragment backFragment;

    private boolean first;

    public QuadStackCardFragmentAdapter(FragmentManager fm, WordInformation wordInformation, QuadStackHandler stackHandler, boolean first) {
        super(fm);

        this.stackHandler = stackHandler;
        this.wordInformation = wordInformation;
        this.first = first;
    }

    @Override
    public Fragment getItem(int position) {
        boolean front = (position == 0);
        return QuadStackCardFragment.newInstance(wordInformation.id, front, first);
    }

    @Override
    public int getCount() {
        return 2; // front + back
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        QuadStackCardFragment createdFragment = (QuadStackCardFragment)super.instantiateItem(container, position);

        if (position == 0)
            frontFragment = createdFragment;
        else if (position == 1)
            backFragment = createdFragment;

        return createdFragment;
    }
}
