package me.cthorne.kioku.test.helpers;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.Utils;
import me.cthorne.kioku.words.WordImage;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 09/02/16.
 */
public class VocabularyComprehension {

    public static TextView getFormTextView(Context context, WordInformation wordInformation) {
        String formKanji = null;
        String formKana = null;

        if (wordInformation.getInformationBytes() != null)
            formKanji = new String(wordInformation.getInformationBytes());
        if (wordInformation.getMetaInformationBytes() != null)
            formKana = new String(wordInformation.getMetaInformationBytes());

        String formString;
        if (formKanji != null) {
            formString = formKanji;

            if (formKana != null)
                formString += " (" + formKana + ")";
        } else {
            formString = formKana;
        }

        TextView formText = new TextView(context);
        formText.setGravity(Gravity.CENTER);
        formText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
        formText.setText(formString);

        return formText;
    }

    public static LinearLayout getTranslationsWithDefinitionsTextView(Context context, DatabaseHelper dbHelper, WordInformation wordInformation) {
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        containerParams.gravity = Gravity.CENTER;

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(containerParams);
        container.setGravity(Gravity.CENTER);

        String translationString = wordInformation.getWord().getTranslationString(dbHelper);

        if (translationString.length() > 0) {
            TextView translationText = new TextView(context);
            translationText.setGravity(Gravity.CENTER);
            translationText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
            translationText.setText(translationString);

            container.addView(translationText);
        }

        String definitionString = wordInformation.getWord().getDefinitionString(dbHelper);

        if (definitionString.length() > 0) {
            TextView definitionText = new TextView(context);
            definitionText.setGravity(Gravity.CENTER);
            definitionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            definitionText.setText(definitionString);

            container.addView(definitionText);
        }

        LinearLayout imageContainer = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        imageContainer.setLayoutParams(params);

        for (WordImage image : wordInformation.getWord().getImages(dbHelper)) {
            ImageView imageView = new ImageView(context);

            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            imageParams.gravity = Gravity.CENTER;
            imageView.setLayoutParams(imageParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Picasso.with(context).load(Utils.mediaFile(context, image.getFileName())).into(imageView);

            imageContainer.addView(imageView);
        }

        if (imageContainer.getChildCount() > 0)
            container.addView(imageContainer);

        // Use the word form itself if no other information is found
        if (container.getChildCount() == 0)
            container.addView(VocabularyComprehension.getFormTextView(context, wordInformation));

        return container;
    }

}
