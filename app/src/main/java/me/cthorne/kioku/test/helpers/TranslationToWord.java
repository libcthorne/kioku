package me.cthorne.kioku.test.helpers;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 09/02/16.
 */
public class TranslationToWord {

    public static TextView getTranslationTextView(Context context, WordInformation wordInformation) {
        TextView translationText = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        translationText.setLayoutParams(params);
        translationText.setGravity(Gravity.CENTER);
        translationText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
        translationText.setText(new String(wordInformation.getInformationBytes()));

        return translationText;
    }

    public static TextView getWordFormsTextView(Context context, DatabaseHelper dbHelper, WordInformation wordInformation) {
        return ImageToWord.getWordStringView(context, dbHelper, wordInformation);
    }

}
