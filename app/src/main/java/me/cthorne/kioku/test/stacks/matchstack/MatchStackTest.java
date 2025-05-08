package me.cthorne.kioku.test.stacks.matchstack;

import android.app.Activity;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import java.sql.SQLException;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.R;
import me.cthorne.kioku.test.stacks.StackTest;

/**
 * Created by chris on 12/01/16.
 */
public abstract class MatchStackTest extends StackTest {

    private MatchStackViewPager viewPager;

    // How many offscreen pages/fragments to load at once
    private static final int OFFSCREEN_PAGE_LIMIT = 3;

    public abstract MatchStackHandler createStackHandler();

    public long countTopWordInformation(DatabaseHelper dbHelper) throws SQLException {
        return dbHelper.countWordInformationForTest(this, getTestWordInformationType());
    }

    public long countTotalBottomWordInformation(DatabaseHelper dbHelper) throws SQLException {
        return dbHelper.countWordInformation(getTestWordInformationType());
    }

    @Override
    public int getContentView() {
        return R.layout.test_match_stack;
    }

    @Override
    public void startTest() {
        Activity activity = getTestActivity();

        MatchStackHandler stackHandler = createStackHandler();

        adapter = new MatchStackFragmentAdapter(activity, ((AppCompatActivity)activity).getSupportFragmentManager(), wordInformationIdsList, stackHandler, this);

        viewPager = (MatchStackViewPager)activity.findViewById(R.id.test_match_stack_pager);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(OFFSCREEN_PAGE_LIMIT);
        viewPager.setPageMargin(0);

        Log.d("kioku-match", "startTest");
    }

    @Override
    public long countNumberOfTests(DatabaseHelper dbHelper) {
        try {
            long top = countTopWordInformation(dbHelper);
            long bottom = countTotalBottomWordInformation(dbHelper);

            Log.d("kioku-count", "[" + getTestType() + "] top:" + top + ",bottom:" + bottom);

            if (top > 0 && bottom > 1)
                return top;
            else
                return 0;
        } catch (SQLException e) {
            Log.e("kioku-db", "canStart: error counting word information");
            return 0;
        }
    }

}
