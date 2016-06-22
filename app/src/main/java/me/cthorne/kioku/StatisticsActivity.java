package me.cthorne.kioku;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import me.cthorne.kioku.statistics.GraphType;
import me.cthorne.kioku.statistics.StatisticsActivityFragmentAdapter;

/**
 * Created by chris on 23/01/16.
 */
public class StatisticsActivity extends AppCompatActivity {

    private static final int OFFSCREEN_PAGE_LIMIT = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        GraphType graphTypes[] = new GraphType[]{GraphType.WORD_COUNT, GraphType.PERFORMANCE, GraphType.STUDY_TIME};

        FragmentPagerAdapter adapter = new StatisticsActivityFragmentAdapter(getSupportFragmentManager(), graphTypes);

        ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(OFFSCREEN_PAGE_LIMIT);
        viewPager.setPageMargin(0);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout)findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_statistics, menu);
        return true;
    }
}