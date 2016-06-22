package me.cthorne.kioku.test.stacks.matchstack;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.sql.SQLException;

import me.cthorne.kioku.KiokuServerClient;
import me.cthorne.kioku.R;
import me.cthorne.kioku.Utils;
import me.cthorne.kioku.test.WordInformationTest;
import me.cthorne.kioku.test.WordInformationTestActivity;
import me.cthorne.kioku.test.stacks.StackFragment;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 12/01/16.
 */
public class MatchStackFragment extends StackFragment {

    private MatchStackItemViewPager bottomViewPager;
    private MatchStackItemFragmentAdapter adapter;

    private RelativeLayout leftButton;
    private RelativeLayout rightButton;

    private boolean first;
    private boolean showedHint;

    static MatchStackFragment newInstance(Integer wordInformationId, boolean currentFragment, boolean first) {
        MatchStackFragment f = new MatchStackFragment();

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
                } catch (SQLException e) {
                    e.printStackTrace();
                    WordInformationTestActivity.finishDbError();
                }

                first = getArguments().getBoolean("first");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout v = (LinearLayout)inflater.inflate(R.layout.test_match_stack_fragment, container, false);

        final MatchStackViewPager thisViewPager = (MatchStackViewPager)container;

        leftButton = (RelativeLayout)v.findViewById(R.id.left_button);
        leftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != MotionEvent.ACTION_DOWN)
                    return false; // only listen for down presses

                bottomViewPager.setCurrentItem(bottomViewPager.getCurrentItem()-1, true);
                return true; // don't forward
            }
        });

        rightButton = (RelativeLayout)v.findViewById(R.id.right_button);
        rightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != MotionEvent.ACTION_DOWN)
                    return false; // only listen for down presses

                bottomViewPager.setCurrentItem(bottomViewPager.getCurrentItem()+1, true);
                return true; // don't forward
            }
        });

        v.setBackgroundColor(Color.WHITE);

        if (wordInformation == null) {
            showLoadingFragment(v);
        } else {
            Log.d("kioku-match", wordInformation.toString());

            // Fill top info

            LinearLayout top = (LinearLayout)v.findViewById(R.id.test_match_stack_fragment_top);
            top.addView(MatchStackFragmentAdapter.stackHandler.getTop(getContext(), wordInformation));
            top.setBackgroundColor(Color.WHITE);

            // Initialise answer button

            View confirmButton = v.findViewById(R.id.test_match_stack_fragment_confirm_button);
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    thisViewPager.checkAnswer();
                    hideButtons();

                    if (first && showedHint)
                        KiokuServerClient.getPreferences(getContext()).edit().putBoolean("matchHintSeen", true).commit();
                }
            });

            // Fill bottom view pager

            bottomViewPager = (MatchStackItemViewPager)v.findViewById(R.id.test_match_stack_item_pager_bottom);
            bottomViewPager.setId(Utils.generateViewId()); // This has to be set (and unique) otherwise only the first view pager is drawn (probably something to do with view recycling)
            adapter = thisViewPager.fillBottom(bottomViewPager, WordInformationTest.activity.getHelper(), wordInformation);
            bottomViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    setupButtons();
                }

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });

            if (first) {
                RelativeLayout bottomContainer = (RelativeLayout) v.findViewById(R.id.bottom_container);
                setupHints(bottomContainer);
            }
        }

        setupButtons();

        return v;
    }

    public void setupHints(RelativeLayout topView) {
        //if (!PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("pref_show_hints", true))
        //    return;

        if (KiokuServerClient.getPreferences(getContext()).getBoolean("matchHintSeen", false))
            return;

        showedHint = true;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

        TextView hint = new TextView(getContext());

        int hintPadding = getResources().getDimensionPixelSize(R.dimen.quad_stack_hint_padding);

        hint.setGravity(Gravity.CENTER);
        hint.setPadding(hintPadding, hintPadding, hintPadding, hintPadding);
        hint.setText(getText(R.string.match_test_hint));
        hint.setBackgroundColor(Color.parseColor("#108DB6"));
        hint.setTextColor(Color.parseColor("#FFFFFF"));

        topView.addView(hint, params);

    }

    private void setupButtons() {
        if (wordInformation == null) { // loading fragment
            hideButtons();
        } else {
            int position = bottomViewPager.getCurrentItem();
            int maxPosition = adapter.getCount() - 1;

            Log.d("kioku-match", "setupButtons " + position + "/" + maxPosition);

            if (position == 0)
                leftButton.setVisibility(View.GONE);
            else
                leftButton.setVisibility(View.VISIBLE);

            if (position == maxPosition)
                rightButton.setVisibility(View.GONE);
            else
                rightButton.setVisibility(View.VISIBLE);
        }
    }

    private void hideButtons() {
        leftButton.setVisibility(View.GONE);
        rightButton.setVisibility(View.GONE);
    }

    public WordInformation getWordInformation() {
        return wordInformation;
    }

    public MatchStackItemViewPager getBottomViewPager() {
        return bottomViewPager;
    }

    @Override
    public void onFragmentShow() {
        super.onFragmentShow();
        Log.d("kioku-match", "MatchStackFragment:onFragmentShow");
        MatchStackFragmentAdapter.stackHandler.onShow(wordInformation);
    }
}
