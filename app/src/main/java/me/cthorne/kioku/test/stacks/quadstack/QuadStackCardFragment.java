package me.cthorne.kioku.test.stacks.quadstack;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
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
import me.cthorne.kioku.test.WordInformationTest;
import me.cthorne.kioku.test.WordInformationTestActivity;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 19/02/16.
 */
public class QuadStackCardFragment extends Fragment {

    private RelativeLayout leftButton;
    private RelativeLayout rightButton;
    public RelativeLayout upButton;
    public RelativeLayout downButton;

    private LinearLayout cardContainer;

    private QuadStackViewPager stackViewPager;

    private boolean front; // Is this fragment the front or backside of a card
    private boolean first; // Is this the first card in the stack
    private WordInformation wordInformation;

    static QuadStackCardFragment newInstance(Integer wordInformationId, boolean front, boolean first) {
        QuadStackCardFragment f = new QuadStackCardFragment();

        Bundle args = new Bundle();
        args.putInt("wordInformationId", wordInformationId);
        args.putBoolean("front", front);
        args.putBoolean("first", first);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() == null)
            return;

        stackViewPager = (QuadStackViewPager)getActivity().findViewById(QuadStackTest.stackViewPagerId);

        int wordInformationId = getArguments().getInt("wordInformationId");

        try {
            wordInformation = WordInformationTest.getHelper().getWordInformationDao().queryForId(wordInformationId);
        } catch (SQLException e) {
            e.printStackTrace();
            WordInformationTestActivity.finishDbError();
            return;
        }

        front = getArguments().getBoolean("front");
        first = getArguments().getBoolean("first");
    }

    private void setupHints(RelativeLayout topView) {
        if (KiokuServerClient.getPreferences(getContext()).getBoolean("quadHintSeen", false))
            return;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView hint = new TextView(getContext());

        int hintPadding = getResources().getDimensionPixelSize(R.dimen.quad_stack_hint_padding);

        hint.setGravity(Gravity.CENTER);
        hint.setPadding(hintPadding, hintPadding, hintPadding, hintPadding);
        hint.setBackgroundColor(Color.parseColor("#108DB6"));
        hint.setTextColor(Color.parseColor("#FFFFFF"));

        if (front) {
            hint.setText(getText(R.string.quad_test_hint_front));
        } else {
            hint.setText(getText(R.string.quad_test_hint_back));
        }

        topView.addView(hint, params);
    }

    private void setupButtons() {
        if (front) {
            rightButton.setVisibility(View.VISIBLE);
        } else {
            leftButton.setVisibility(View.VISIBLE);

            upButton.setVisibility(View.VISIBLE);
            upButton.setOnTouchListener(new NonSwipeTouchListener(true));

            downButton.setVisibility(View.VISIBLE);
            downButton.setOnTouchListener(new NonSwipeTouchListener(false));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout topView = (RelativeLayout)inflater.inflate(R.layout.test_quad_stack_card_fragment, container, false);

        cardContainer = (LinearLayout)topView.findViewById(R.id.container);
        View cardContent = front ? QuadStackFragmentAdapter.stackHandler.getFront(getContext(), wordInformation) :
                                    QuadStackFragmentAdapter.stackHandler.getBack(getContext(), wordInformation);
        cardContainer.addView(cardContent);

        leftButton = (RelativeLayout)topView.findViewById(R.id.left_button);
        rightButton = (RelativeLayout)topView.findViewById(R.id.right_button);
        upButton = (RelativeLayout)topView.findViewById(R.id.up_button);
        downButton = (RelativeLayout)topView.findViewById(R.id.down_button);

        if (first)
            setupHints(topView);

        setupButtons();

        if (front) {
            topView.setTag(QuadStackViewPager.TAG_QUESTION);
            topView.setBackgroundColor(Color.WHITE); // front and loading fragments are white
        } else {
            topView.setTag(QuadStackViewPager.TAG_ANSWER);
            topView.setBackgroundColor(Color.LTGRAY); // answer fragments are gray
        }

        return topView;
    }

    public WordInformation getWordInformation() {
        return wordInformation;
    }

    private class NonSwipeTouchListener implements View.OnTouchListener {
        private boolean nextDirection;
        private float startX;
        private float startY;

        public NonSwipeTouchListener(boolean nextDirection) {
            this.nextDirection = nextDirection;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!stackViewPager.isShowingBack())
                return false;

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // Save starts coords of press
                startX = event.getX();
                startY = event.getY();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                // Check if press was a press and not a swipe
                if (Math.abs(event.getX()-startX) < QuadStackViewPager.SWIPE_THRESHOLD && Math.abs(event.getY()-startY) < QuadStackViewPager.SWIPE_THRESHOLD) {
                    stackViewPager.showNext(nextDirection);
                    return true; // don't forward up press to view pager/touch handler
                }
            }

            if (!stackViewPager.getCurrentFragment().isLoadingFragment)
                stackViewPager.getCurrentFragment().cardViewPager.onTouchEvent(event);

            return true; // forwarding of events is handled above
        }
    }
}
