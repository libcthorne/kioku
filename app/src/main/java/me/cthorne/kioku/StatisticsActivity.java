package me.cthorne.kioku;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import me.cthorne.kioku.orm.OrmLiteBaseActivityCompat;
import me.cthorne.kioku.statistics.GraphType;
import me.cthorne.kioku.statistics.StatisticsActivityFragment;
import me.cthorne.kioku.statistics.StatisticsActivityFragmentAdapter;

/**
 * Created by chris on 24/01/16.
 */
public class StatisticsActivity extends OrmLiteBaseActivityCompat<DatabaseHelper> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // Display up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Define the graph types to display
        GraphType[] graphTypes = new GraphType[]{
            GraphType.WORD_COUNT,
            GraphType.PERFORMANCE,
            GraphType.STUDY_TIME
        };

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new StatisticsActivityFragmentAdapter(getSupportFragmentManager(), graphTypes));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
