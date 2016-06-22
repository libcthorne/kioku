package me.cthorne.kioku.languages;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

import me.cthorne.kioku.R;

/**
 * Created by chris on 10/02/16.
 */
public class AddLanguagesListViewAdapter extends BaseAdapter {
    Activity activity;
    ArrayList<SelectableWordLanguage> languages;

    public AddLanguagesListViewAdapter(Activity activity, ArrayList<SelectableWordLanguage> languages) {
        super();
        this.activity = activity;
        this.languages = languages;
    }

    @Override
    public int getCount() {
        return languages.size();
    }

    @Override
    public Object getItem(int position) {
        return languages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();

        if (convertView == null)
            convertView = inflater.inflate(R.layout.row_with_checkbox, null);

        final SelectableWordLanguage language = (SelectableWordLanguage)getItem(position);

        Log.d("kioku-languages", "language " + language.getLanguage() + " selected = " + language.isSelected() + " (Adapter)");

        // TextView for name of language
        TextView nameTextView = (TextView)convertView.findViewById(R.id.row_name);
        String name = language.getLanguage().toString();
        nameTextView.setText(name);

        // CheckBox for whether language is selected or not
        final CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.row_checkbox);
        checkBox.setChecked(language.isSelected());

        // Click on text -> toggle checkbox
        nameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox.toggle();
            }
        });

        // Listen to checkbox changes
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("kioku-languages", "check changed");
                language.setSelected(isChecked);
            }
        });

        return convertView;
    }
}
