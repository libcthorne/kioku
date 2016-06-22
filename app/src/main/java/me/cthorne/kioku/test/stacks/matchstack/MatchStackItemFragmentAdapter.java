package me.cthorne.kioku.test.stacks.matchstack;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 12/01/16.
 */
public class MatchStackItemFragmentAdapter extends FragmentPagerAdapter {
    private ArrayList<WordInformation> wordInformationList;
    private MatchStackHandler stackHandler;
    private int correctAnswerIndex;

    public MatchStackItemFragmentAdapter(DatabaseHelper dbHelper, FragmentManager fm, WordInformation wordInformation, MatchStackHandler stackHandler) {
        super(fm);

        this.stackHandler = stackHandler;
        this.wordInformationList = stackHandler.getBottomOptions(dbHelper, wordInformation);
        this.correctAnswerIndex = wordInformationList.indexOf(wordInformation);
    }

    @Override
    public Fragment getItem(int position) {
        WordInformation wordInformation = wordInformationList.get(position);

        return MatchStackItemFragment.newInstance(wordInformation.id);
    }

    @Override
    public int getCount() {
        return wordInformationList.size();
    }

    public int getCorrectAnswerPosition() {
        return correctAnswerIndex;
    }
}
