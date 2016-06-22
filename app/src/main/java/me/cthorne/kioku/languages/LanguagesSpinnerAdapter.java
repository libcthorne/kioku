package me.cthorne.kioku.languages;

import android.app.Activity;
import android.widget.ArrayAdapter;

import java.util.List;

import me.cthorne.kioku.R;
import me.cthorne.kioku.words.WordLanguage;

/**
 * Created by chris on 10/02/16.
 */
public class LanguagesSpinnerAdapter extends ArrayAdapter {

    private Activity activity;
    private List<WordLanguage> languages;

    public LanguagesSpinnerAdapter(Activity activity, int textViewResourceId, List<WordLanguage> languages) {
        super(activity, textViewResourceId);

        this.activity = activity;
        this.languages = languages;
    }

    @Override
    public int getCount() {
        return languages.size()+1; // +1 for "+ add language"
    }

    @Override
    public Object getItem(int position) {
        if (position == languages.size())
            return activity.getString(R.string.spinner_add_language);

        return languages.get(position);
    }

}