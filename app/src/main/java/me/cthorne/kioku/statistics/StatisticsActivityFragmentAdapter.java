package me.cthorne.kioku.statistics;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

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
