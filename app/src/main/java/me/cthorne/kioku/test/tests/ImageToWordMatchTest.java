package me.cthorne.kioku.test.tests;

import android.content.Context;
import android.view.View;

import me.cthorne.kioku.MainActivity;
import me.cthorne.kioku.test.WordInformationTestType;
import me.cthorne.kioku.test.helpers.ImageToWord;
import me.cthorne.kioku.test.stacks.matchstack.MatchStackHandler;
import me.cthorne.kioku.test.stacks.matchstack.MatchStackTest;
import me.cthorne.kioku.words.WordInformation;
import me.cthorne.kioku.words.WordInformationType;

/**
 * Created by chris on 12/01/16.
 */
public class ImageToWordMatchTest extends MatchStackTest {

    @Override
    public MatchStackHandler createStackHandler() {
        return new MatchStackHandler() {
            @Override
            public View getTop(Context context, WordInformation wordInformation) {
                return ImageToWord.getImageView(context, wordInformation);
            }

            @Override
            public View getBottom(Context context, WordInformation wordInformation) {
                return ImageToWord.getWordStringView(context, getHelper(), wordInformation);
            }
        };
    }

    @Override
    public WordInformationTestType getTestType() {
        return WordInformationTestType.VOCABULARY_RECALL;
    }

    @Override
    public WordInformationType getTestWordInformationType() {
        return WordInformationType.IMAGE;
    }

    @Override
    public void processCorrectAnswer(WordInformation wordInformation, float secondsTaken) {
        super.processCorrectAnswer(wordInformation, secondsTaken);
        MainActivity.ttsSpeak(wordInformation.getWord().getWordStringKanaPreferred(getHelper()));
    }

    @Override
    public void processIncorrectAnswer(WordInformation wordInformation, float secondsTaken) {
        super.processIncorrectAnswer(wordInformation, secondsTaken);
        MainActivity.ttsSpeak(wordInformation.getWord().getWordStringKanaPreferred(getHelper()));
    }
}
