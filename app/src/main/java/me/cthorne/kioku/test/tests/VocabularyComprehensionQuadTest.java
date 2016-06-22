package me.cthorne.kioku.test.tests;

import android.content.Context;
import android.view.View;

import me.cthorne.kioku.MainActivity;
import me.cthorne.kioku.test.WordInformationTestType;
import me.cthorne.kioku.test.helpers.VocabularyComprehension;
import me.cthorne.kioku.test.stacks.quadstack.QuadStackHandler;
import me.cthorne.kioku.test.stacks.quadstack.QuadStackTest;
import me.cthorne.kioku.words.WordInformation;
import me.cthorne.kioku.words.WordInformationType;

/**
 * Created by chris on 07/01/16.
 */
public class VocabularyComprehensionQuadTest extends QuadStackTest {

    @Override
    public QuadStackHandler createStackHandler() {
        return new QuadStackHandler() {
            @Override
            public View getFront(Context context, WordInformation wordInformation) {
                return VocabularyComprehension.getFormTextView(context, wordInformation);
            }

            @Override
            public View getBack(Context context, WordInformation wordInformation) {
                return VocabularyComprehension.getTranslationsWithDefinitionsTextView(context, getHelper(), wordInformation);
            }

            @Override
            public void onShow(WordInformation wordInformation, boolean front) {
                if (!front)
                    return;

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
