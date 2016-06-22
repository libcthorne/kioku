package me.cthorne.kioku.test.stacks.matchstack;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.sql.SQLException;

import me.cthorne.kioku.R;
import me.cthorne.kioku.test.WordInformationTest;
import me.cthorne.kioku.test.WordInformationTestActivity;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 12/01/16.
 */
public class MatchStackItemFragment extends Fragment {
    private WordInformation wordInformation;

    static MatchStackItemFragment newInstance(Integer wordInformationId) {
        MatchStackItemFragment f = new MatchStackItemFragment();

        Bundle args = new Bundle();
        args.putInt("wordInformationId", wordInformationId);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            int wordInformationId = getArguments().getInt("wordInformationId");

            try {
                wordInformation = WordInformationTest.getHelper().getWordInformationDao().queryForId(wordInformationId);
            } catch (SQLException e) {
                e.printStackTrace();
                WordInformationTestActivity.finishDbError();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout v = (LinearLayout)inflater.inflate(R.layout.test_match_stack_item_fragment, container, false);

        v.setBackgroundColor(Color.WHITE);
        v.addView(MatchStackFragmentAdapter.stackHandler.getBottom(getContext(), wordInformation));

        return v;
    }

    public WordInformation getWordInformation() {
        return wordInformation;
    }
}
