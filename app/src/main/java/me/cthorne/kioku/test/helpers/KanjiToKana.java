package me.cthorne.kioku.test.helpers;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 09/02/16.
 */
public class KanjiToKana {

    public static View getKanjiTextView(Context context, WordInformation wordInformation) {
        String kanjiString = new String(wordInformation.getInformationBytes());

        TextView kanjiText = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        kanjiText.setLayoutParams(params);
        kanjiText.setGravity(Gravity.CENTER);
        kanjiText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
        kanjiText.setText(kanjiString);

        return kanjiText;
    }
    public static View getKanaTextView(Context context, WordInformation wordInformation) {
        byte[] meta = wordInformation.getMetaInformationBytes();
        String kanaString = (meta != null ? new String(meta) : "");

        TextView kanaText = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        kanaText.setLayoutParams(params);
        kanaText.setGravity(Gravity.CENTER);
        kanaText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
        kanaText.setText(kanaString);

        return kanaText;
    }

}
