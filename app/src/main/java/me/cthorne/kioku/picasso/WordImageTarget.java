package me.cthorne.kioku.picasso;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by chris on 06/01/16.
 */
public class WordImageTarget implements Target {

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        Log.d("picasso", "bitmap loaded");
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        Log.d("picasso", "bitmap failed");
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        Log.d("picasso", "prepare load");
    }

}
