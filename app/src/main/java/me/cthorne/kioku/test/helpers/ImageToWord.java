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
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 09/02/16.
 */
public class ImageToWord {

    public static ImageView getImageView(Context context, WordInformation wordInformation) {
        ImageView imageView = new ImageView(context);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        String imageFileName = new String(wordInformation.getInformationBytes());
        Picasso.with(context).load(Utils.mediaFile(context, imageFileName)).into(imageView);

        return imageView;
    }

    public static TextView getWordStringView(Context context, DatabaseHelper dbHelper, WordInformation wordInformation) {
        TextView japaneseText = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        japaneseText.setLayoutParams(params);
        japaneseText.setGravity(Gravity.CENTER);
        japaneseText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
        japaneseText.setText(wordInformation.getWord().getWordString(dbHelper));

        return japaneseText;
    }

}
