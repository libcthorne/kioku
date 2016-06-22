package me.cthorne.kioku.test.tests;

import android.content.Context;
import android.view.View;

import me.cthorne.kioku.MainActivity;
import me.cthorne.kioku.test.WordInformationTestType;
import me.cthorne.kioku.test.helpers.VocabularyComprehension;
import me.cthorne.kioku.test.stacks.matchstack.MatchStackHandler;
import me.cthorne.kioku.test.stacks.matchstack.MatchStackTest;
import me.cthorne.kioku.words.WordInformation;
import me.cthorne.kioku.words.WordInformationType;

/**
 * Created by chris on 09/02/16.
 */
public class VocabularyComprehensionMatchTest extends MatchStackTest {

    @Override
    public MatchStackHandler createStackHandler() {
        return new MatchStackHandler() {
            @Override
            public View getTop(Context context, WordInformation wordInformation) {
                return VocabularyComprehension.getFormTextView(context, wordInformation);
            }

            @Override
            public View getBottom(Context context, WordInformation wordInformation) {
                return VocabularyComprehension.getTranslationsWithDefinitionsTextView(context, getHelper(), wordInformation);
            }

            @Override
            public void onShow(WordInformation wordInformation) {
                MainActivity.ttsSpeak(wordInformation);
            }
        };
    }

    @Override
    public WordInformationTestType getTestType() {
        return WordInformationTestType.VOCABULARY_COMPREHENSION;
    }

    @Override
    public WordInformationType getTestWordInformationType() {
        return WordInformationType.WORD_FORM;
    }

}
