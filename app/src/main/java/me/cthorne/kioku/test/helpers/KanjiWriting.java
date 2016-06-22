package me.cthorne.kioku.test.helpers;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 18/02/16.
 */
public class KanjiWriting {

    public static LinearLayout getKanjiPromptTextView(Context context, DatabaseHelper dbHelper, WordInformation wordInformation) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;

        String kanaString = wordInformation.getMetaInformationBytes() != null ? new String(wordInformation.getMetaInformationBytes()) : null;
        if (kanaString != null) {
            TextView kanaTextView = new TextView(context);
            kanaTextView.setGravity(Gravity.CENTER);
            kanaTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
            kanaTextView.setText(kanaString);

            container.addView(kanaTextView, params);
        }

        String translationString = wordInformation.getWord().getTranslationString(dbHelper);
        if (translationString.length() > 0) {
            TextView translationTextView = new TextView(context);
            translationTextView.setGravity(Gravity.CENTER);
            translationTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            translationTextView.setText(translationString);

            container.addView(translationTextView, params);
        }

        String definitionString = wordInformation.getWord().getDefinitionString(dbHelper);
        if (definitionString.length() > 0) {
            TextView definitionTextView = new TextView(context);
            definitionTextView.setGravity(Gravity.CENTER);
            definitionTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            definitionTextView.setText(definitionString);

            container.addView(definitionTextView, params);
        }

        return container;
    }

    public static TextView getKanjiTextView(Context context, WordInformation wordInformation) {
        String kanjiString = new String(wordInformation.getInformationBytes());

        TextView kanjiTextView = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        kanjiTextView.setLayoutParams(params);
        kanjiTextView.setGravity(Gravity.CENTER);
        kanjiTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
        kanjiTextView.setText(kanjiString);

        return kanjiTextView;
    }

}
