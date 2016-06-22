package me.cthorne.kioku.test.helpers;

import android.content.Context;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 18/02/16.
 */
public class Sentence {

    public static TextView getSentenceTextView(Context context, WordInformation wordInformation) {
        String sentence = new String(wordInformation.getInformationBytes());

        TextView sentenceTextView = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        sentenceTextView.setLayoutParams(params);
        sentenceTextView.setGravity(Gravity.CENTER);
        sentenceTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        sentenceTextView.setText(sentence);

        return sentenceTextView;
    }

    public static TextView getSentenceTranslationTextView(Context context, DatabaseHelper dbHelper, WordInformation wordInformation) {
        String sentenceTranslation;

        if (wordInformation.getMetaInformationBytes() != null) {
            sentenceTranslation = new String(wordInformation.getMetaInformationBytes());
        } else { // use word string and definition if no translation is found
            String wordString = wordInformation.getWord().getWordString(dbHelper);
            String definitionString = wordInformation.getWord().getDefinitionString(dbHelper);

            sentenceTranslation = "";

            if (!wordString.isEmpty())
                sentenceTranslation += "<i>" + wordString + "</i><br/>";

            if (!definitionString.isEmpty())
                sentenceTranslation += definitionString;
        }


        TextView translationTextView = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        translationTextView.setLayoutParams(params);
        translationTextView.setGravity(Gravity.CENTER);
        translationTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        translationTextView.setText(Html.fromHtml(sentenceTranslation));

        return translationTextView;
    }

}
