package me.cthorne.kioku.test.tests;

import android.content.Context;
import android.view.View;

import me.cthorne.kioku.MainActivity;
import me.cthorne.kioku.test.WordInformationTestType;
import me.cthorne.kioku.test.helpers.KanjiToKana;
import me.cthorne.kioku.test.stacks.quadstack.QuadStackHandler;
import me.cthorne.kioku.test.stacks.quadstack.QuadStackTest;
import me.cthorne.kioku.words.WordInformation;
import me.cthorne.kioku.words.WordInformationType;

/**
 * Created by chris on 30/01/16.
 */
public class KanjiToKanaQuadTest extends QuadStackTest {

    @Override
    public QuadStackHandler createStackHandler() {
        return new QuadStackHandler() {
            @Override
            public View getFront(Context context, WordInformation wordInformation) {
                return KanjiToKana.getKanjiTextView(context, wordInformation);
            }

            @Override
            public View getBack(Context context, WordInformation wordInformation) {
                return KanjiToKana.getKanaTextView(context, wordInformation);
            }

            @Override
            public void onShow(WordInformation wordInformation, boolean front) {
                if (front)
                    return;

                MainActivity.ttsSpeak(wordInformation);
            }
        };
    }

    @Override
    public WordInformationTestType getTestType() {
        return WordInformationTestType.KANJI_READING;
    }

    @Override
    public WordInformationType getTestWordInformationType() {
        return WordInformationType.WORD_FORM;
    }

    @Override
    public String getWhereConditionsForTesting() {
        // Only test forms with kanji and kana
        return "(" + super.getWhereConditionsForTesting() + ") AND " +
            "`word_informations`.`informationBytes` IS NOT NULL AND `word_informations`.`metaInformationBytes` IS NOT NULL";
    }

}

