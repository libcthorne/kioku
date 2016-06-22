package me.cthorne.kioku.infosources;

import android.app.Activity;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import me.cthorne.kioku.R;

/**
 * Created by chris on 26/01/16.
 */
public class SourcesListViewAdapter extends BaseAdapter {

    Activity activity;
    ArrayList<SelectedWordInformationSource> sources;

    public SourcesListViewAdapter(Activity activity, ArrayList<SelectedWordInformationSource> sources) {
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
        return 0; // not used
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();

        if (convertView == null)
            convertView = inflater.inflate(R.layout.source_row, null);

        TextView nameTextView = (TextView)convertView.findViewById(R.id.row_name);
        final SelectedWordInformationSource selectedSource = (SelectedWordInformationSource)getItem(position);

        String name = selectedSource.getSource().getTitle();
        String url = selectedSource.getSource().getUrl();
        String enabledStr = selectedSource.isEnabled() ? "Enabled" : "Disabled";
        String s = name;
        //s += "\n" + url;
        s += "\n" + enabledStr;

        SpannableString ss1 = new SpannableString(s);

        int strPos = 0;
        // size of name
        ss1.setSpan(new RelativeSizeSpan(1f), strPos, strPos+name.length(), 0);
        //ss1.setSpan(new ForegroundColorSpan(Color.RED), 0, 5, 0);
        strPos += name.length()+1;

        // size and colour of url
        /*ss1.setSpan(new RelativeSizeSpan(0.5f), strPos, strPos+url.length(), 0);
        ss1.setSpan(new ForegroundColorSpan(Color.BLUE), strPos, strPos+url.length(), 0);
        strPos += url.length()+1;*/

        // size and colour of enabled/disabled string
        ss1.setSpan(new RelativeSizeSpan(0.5f), strPos, strPos+enabledStr.length(), 0);
        ss1.setSpan(new ForegroundColorSpan(selectedSource.isEnabled() ? Color.BLUE : Color.GRAY), strPos, strPos+enabledStr.length(), 0);

        nameTextView.setText(ss1);

        return convertView;
    }
}
