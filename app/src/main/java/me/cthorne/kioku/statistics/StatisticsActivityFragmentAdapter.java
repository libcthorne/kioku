package me.cthorne.kioku.statistics;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * Created by chris on 29/01/16.
 */
public class StatisticsActivityFragmentAdapter extends FragmentPagerAdapter {

    private GraphType[] graphTypes;

    public StatisticsActivityFragmentAdapter(FragmentManager fm, GraphType[] graphTypes) {
        super(fm);

        this.graphTypes = graphTypes;
    }

    @Override
    public Fragment getItem(int position) {
        return StatisticsActivityFragment.newInstance(graphTypes[position]);
    }

    @Override
    public int getCount() {
        return graphTypes.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return graphTypes[position].toString();
    }
}
