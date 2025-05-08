package me.cthorne.kioku.infosources;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

import me.cthorne.kioku.R;

/**
 * Created by chris on 26/01/16.
 */
public class SourcesListViewAdapter extends DragItemAdapter<SelectedWordInformationSource, SourcesListViewAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<SelectedWordInformationSource> sources;

    public SourcesListViewAdapter(Activity activity, ArrayList<SelectedWordInformationSource> sources) {
        super();
        this.activity = activity;
        this.sources = sources;
        setItemList(sources);
    }

    @Override
    public long getUniqueItemId(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.source_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final SelectedWordInformationSource selectedSource = sources.get(position);

        String name = selectedSource.getSource().getTitle();
        String url = selectedSource.getSource().getUrl();
        String enabledStr = selectedSource.isEnabled() ? "Enabled" : "Disabled";
        String s = name;
        s += "\n" + enabledStr;

        SpannableString ss1 = new SpannableString(s);

        int strPos = 0;
        // size of name
        ss1.setSpan(new RelativeSizeSpan(1f), strPos, strPos+name.length(), 0);
        strPos += name.length()+1;

        // size and colour of enabled/disabled string
        ss1.setSpan(new RelativeSizeSpan(0.5f), strPos, strPos+enabledStr.length(), 0);
        ss1.setSpan(new ForegroundColorSpan(selectedSource.isEnabled() ? Color.BLUE : Color.GRAY), strPos, strPos+enabledStr.length(), 0);

        holder.nameTextView.setText(ss1);
        holder.selectedSource = selectedSource;
    }

    public class ViewHolder extends DragItemAdapter.ViewHolder {
        public TextView nameTextView;
        public SelectedWordInformationSource selectedSource;

        public ViewHolder(final View itemView) {
            super(itemView, R.id.row_name, false);
            nameTextView = (TextView) itemView.findViewById(R.id.row_name);
        }
        
        @Override
        public void onItemClicked(View view) {
            Intent viewIntent = new Intent(activity, EditSourceActivity.class);
            viewIntent.putExtra("selectedSourceId", selectedSource.id);
            activity.startActivity(viewIntent);
        }
    }
}
