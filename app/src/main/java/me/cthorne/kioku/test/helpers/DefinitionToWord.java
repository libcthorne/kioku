package me.cthorne.kioku.test.helpers;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 23/02/16.
 */
public class DefinitionToWord {

    public static TextView getDefinitionTextView(Context context, WordInformation wordInformation) {
        TextView definitionText = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        definitionText.setLayoutParams(params);
        definitionText.setGravity(Gravity.CENTER);
        definitionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        definitionText.setText(new String(wordInformation.getInformationBytes()));

        return definitionText;
    }

    public static TextView getWordFormsTextView(Context context, DatabaseHelper dbHelper, WordInformation wordInformation) {
        return ImageToWord.getWordStringView(context, dbHelper, wordInformation);
    }
}
