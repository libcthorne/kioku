package me.cthorne.kioku.infosources;

/**
 * Created by chris on 23/01/16.
 */

import android.app.Activity;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import me.cthorne.kioku.R;

/**
 * Created by chris on 15/11/15.
 */
public class AddSourcesListViewAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<SelectableWordInformationSource> sources;

    public AddSourcesListViewAdapter(Activity activity, ArrayList<SelectableWordInformationSource> sources) {
        super();
        this.activity = activity;
        this.sources = sources;
    }

    @Override
    public int getCount() {
        return sources.size();
    }

    @Override
    public Object getItem(int position) {
        return sources.get(position);
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

        TextView nameTextView = (TextView)convertView.findViewById(R.id.row_name);
        final CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.row_checkbox);

        final SelectableWordInformationSource source = (SelectableWordInformationSource)getItem(position);

        String name = source.getSource().getTitle();
        String url = source.getSource().getUrl().replace("[WORD]", "*"); // Show [WORD] as * for aesthetic reasons
        String s = name + "\n" + url;
        SpannableString ss1 = new SpannableString(s);

        // size of name
        ss1.setSpan(new RelativeSizeSpan(1f), 0, name.length(), 0);
        //ss1.setSpan(new ForegroundColorSpan(Color.RED), 0, 5, 0);

        // size and colour of url
        ss1.setSpan(new RelativeSizeSpan(0.5f), name.length()+1, name.length()+1+url.length(), 0);
        ss1.setSpan(new ForegroundColorSpan(Color.BLUE), name.length()+1, name.length()+1+url.length(), 0);

        nameTextView.setText(ss1);
        //nameTextView.setText(source.getName());

        // Click on text -> toggle checkbox
        nameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox.toggle();
            }
        });

        // Disable sources that the user already has
        if (source.isDisabled()) {
            checkBox.setChecked(true);
            convertView.setAlpha(0.3f);
            checkBox.setClickable(false);
            nameTextView.setClickable(false);
        }

        // Listen to checkbox changes
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("kioku-sources", "check changed");
                source.setSelected(isChecked);
            }
        });


        // Check recommended sources by default
        if (source.isRecommended()) {
            checkBox.setChecked(true);
        }

        return convertView;
    }
}

