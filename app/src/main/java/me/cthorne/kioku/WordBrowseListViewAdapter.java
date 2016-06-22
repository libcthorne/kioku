package me.cthorne.kioku;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import me.cthorne.kioku.words.Word;

/**
 * Created by chris on 15/11/15.
 */
public class WordBrowseListViewAdapter extends BaseAdapter {

    private Activity activity;
    private DatabaseHelper dbHelper;
    private ArrayList<Word> words;

    public WordBrowseListViewAdapter(Activity activity, DatabaseHelper dbHelper, ArrayList<Word> words) {
        super();
        this.activity = activity;
        this.dbHelper = dbHelper;
        this.words = words;
    }

    @Override
    public int getCount() {
        return words.size();
    }

    @Override
    public Object getItem(int position) {
        return words.get(position);
    }

    @Override
    public long getItemId(int position) {
        return ((Word)getItem(position)).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();

        if (convertView == null)
            convertView = inflater.inflate(R.layout.word_browse_row, null);

        TextView wordTextView = (TextView)convertView.findViewById(R.id.row_word);
        /*TextView wordMeaningNextDueTextView = (TextView)convertView.findViewById(R.id.row_word_meaning_next_due);
        TextView wordProductionNextDueTextView = (TextView)convertView.findViewById(R.id.row_word_production_next_due);
        TextView wordDebugTextView = (TextView)convertView.findViewById(R.id.row_word_debug);*/

        Word word = (Word)getItem(position);
        wordTextView.setText(word.getWordString(dbHelper, ", ", "?"));

        return convertView;
    }
}
