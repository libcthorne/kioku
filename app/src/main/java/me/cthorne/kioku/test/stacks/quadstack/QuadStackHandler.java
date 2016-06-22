package me.cthorne.kioku.test.stacks.quadstack;

import android.content.Context;
import android.view.View;

import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 26/11/15.
 */
public abstract class QuadStackHandler {
    public abstract View getFront(Context context, WordInformation wordInformation);
    public abstract View getBack(Context context, WordInformation wordInformation);
    public void onShow(WordInformation wordInformation, boolean front) {}
}
