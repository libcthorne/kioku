package me.cthorne.kioku.test.stacks.quadstack;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.sql.SQLException;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.R;
import me.cthorne.kioku.SM2;
import me.cthorne.kioku.Utils;
import me.cthorne.kioku.test.stacks.StackTest;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 07/01/16.
 */
public abstract class QuadStackTest extends StackTest {

    private QuadStackViewPager viewPager;

    // How many pages/fragments to load at once
    private static final int OFFSCREEN_PAGE_LIMIT = 5;

    public static int stackViewPagerId;

    public abstract QuadStackHandler createStackHandler();

    @Override
    public int getContentView() {
        return R.layout.test_quad_stack;
    }

    @Override
    public void startTest() {
        Activity activity = getTestActivity();

        QuadStackHandler stackHandler = createStackHandler();

        adapter = new QuadStackFragmentAdapter(activity, ((AppCompatActivity)activity).getSupportFragmentManager(), wordInformationIdsList, stackHandler, this);

        //viewPager = (QuadStackViewPager)activity.findViewById(R.id.test_quad_stack_pager);
        stackViewPagerId = Utils.generateViewId();

        viewPager = new QuadStackViewPager(activity);
        viewPager.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        viewPager.setId(stackViewPagerId); // This has to be set (and unique) otherwise only the first view pager is drawn (probably something to do with view recycling)
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(OFFSCREEN_PAGE_LIMIT);
        viewPager.setPageMargin(0);

        RelativeLayout container = (RelativeLayout)activity.findViewById(R.id.test_quad_directional_container);
        container.addView(viewPager);

        adapter.notifyDataSetChanged();
    }

    @Override
    public void finishTest() {
        super.finishTest();

        ((ViewGroup)viewPager.getParent()).removeView(viewPager);
    }

    @Override
    public long countNumberOfTests(DatabaseHelper dbHelper) {
        try {
            return dbHelper.countWordInformationForTest(this, getTestWordInformationType());
        } catch (SQLException e) {
            Log.e("kioku-db", "canStart: error counting word information");
            return 0;
        }
    }

    public void processCorrectAnswer(WordInformation wordInformation, float secondsTaken) {
        super.processCorrectAnswer(wordInformation, secondsTaken);
        dbSaveAnswer(wordInformation, SM2.CORRECT_ANSWER, secondsTaken);
    }

    public void processIncorrectAnswer(WordInformation wordInformation, float secondsTaken) {
        super.processIncorrectAnswer(wordInformation, secondsTaken);
        dbSaveAnswer(wordInformation, SM2.INCORRECT_ANSWER, secondsTaken);
    }

}
