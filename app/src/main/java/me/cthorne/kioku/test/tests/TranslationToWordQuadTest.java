package me.cthorne.kioku.test.tests;

import android.content.Context;
import android.view.View;

import me.cthorne.kioku.MainActivity;
import me.cthorne.kioku.test.WordInformationTestType;
import me.cthorne.kioku.test.helpers.TranslationToWord;
import me.cthorne.kioku.test.stacks.quadstack.QuadStackHandler;
import me.cthorne.kioku.test.stacks.quadstack.QuadStackTest;
import me.cthorne.kioku.words.WordInformation;
import me.cthorne.kioku.words.WordInformationType;

/**
 * Created by chris on 07/01/16.
 */
public class TranslationToWordQuadTest extends QuadStackTest {

    @Override
    public QuadStackHandler createStackHandler() {
        return new QuadStackHandler() {
            @Override
            public View getFront(Context context, WordInformation wordInformation) {
                return TranslationToWord.getTranslationTextView(context, wordInformation);
            }

            @Override
            public View getBack(Context context, WordInformation wordInformation) {
                return TranslationToWord.getWordFormsTextView(context, getHelper(), wordInformation);
            }

            @Override
            public void onShow(WordInformation wordInformation, boolean front) {
                if (front)
                    return;

                MainActivity.ttsSpeak(wordInformation.getWord().getWordStringKanaPreferred(getHelper()));
            }
        };
    }

    @Override
    public WordInformationTestType getTestType() {
        return WordInformationTestType.VOCABULARY_RECALL;
    }

    @Override
    public WordInformationType getTestWordInformationType() {
        return WordInformationType.TRANSLATION;
    }

}
