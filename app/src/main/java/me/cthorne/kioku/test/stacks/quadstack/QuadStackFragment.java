package me.cthorne.kioku.test.stacks.quadstack;

import android.graphics.Color;
import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.sql.SQLException;

import me.cthorne.kioku.R;
import me.cthorne.kioku.Utils;
import me.cthorne.kioku.test.WordInformationTest;
import me.cthorne.kioku.test.WordInformationTestActivity;
import me.cthorne.kioku.test.stacks.StackFragment;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 23/11/15.
 */
public class QuadStackFragment extends StackFragment {

    public QuadStackCardViewPager cardViewPager;
    private QuadStackCardFragmentAdapter adapter;
    public boolean isLoadingFragment;
    private boolean first;

    static QuadStackFragment newInstance(Integer wordInformationId, boolean currentFragment, boolean first) {
        QuadStackFragment f = new QuadStackFragment();

        Bundle args = new Bundle();
        if (wordInformationId != null)
            args.putInt("wordInformationId", wordInformationId);
        args.putBoolean("currentFragment", currentFragment);
        args.putBoolean("first", first);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            int wordInformationId = getArguments().getInt("wordInformationId");

            if (wordInformationId != 0) {
                try {
                    wordInformation = WordInformationTest.getHelper().getWordInformationDao().queryForId(wordInformationId);

                    if (getArguments().getBoolean("currentFragment"))
                        onFragmentShow();

                    first = getArguments().getBoolean("first");
                } catch (SQLException e) {
                    e.printStackTrace();
                    WordInformationTestActivity.finishDbError();
                }
            } else {
                isLoadingFragment = true;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout topView = (RelativeLayout)inflater.inflate(R.layout.test_quad_stack_fragment, container, false);

        final QuadStackViewPager thisViewPager = (QuadStackViewPager)container;

        if (wordInformation == null) {
            // Loading
            showLoadingFragment(topView);
            topView.setTag(QuadStackViewPager.TAG_LOADING);
            topView.setBackgroundColor(Color.WHITE); // front and loading fragments are white
        } else {
            // Stack card
            cardViewPager = (QuadStackCardViewPager)topView.findViewById(R.id.test_quad_stack_card_pager);
            cardViewPager.setId(Utils.generateViewId()); // This has to be set (and unique) otherwise only the first view pager is drawn (probably something to do with view recycling)
            cardViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    onFragmentShow(position==0);
                }

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });

            adapter = thisViewPager.fillCard(cardViewPager, wordInformation, first);
        }

        return topView;
    }

    public WordInformation getWordInformation() {
        return wordInformation;
    }

    @Override
    public void onFragmentShow() {
        super.onFragmentShow();

        QuadStackFragmentAdapter.stackHandler.onShow(wordInformation, true);
    }

    public void onFragmentShow(boolean front) {
        super.onFragmentShow();

        QuadStackFragmentAdapter.stackHandler.onShow(wordInformation, front);
    }
}
