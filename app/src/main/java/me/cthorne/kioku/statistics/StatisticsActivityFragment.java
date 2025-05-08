package me.cthorne.kioku.statistics;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.R;
import me.cthorne.kioku.StatisticsActivity;
import me.cthorne.kioku.words.Word;

/**
 * Created by chris on 24/01/16.
 */
public class StatisticsActivityFragment extends Fragment {

    private static final String ARG_GRAPH_TYPE = "graph_type";
    private GraphType graphType;

    /**
     * Create a new instance of the fragment with the given graph type.
     */
    public static StatisticsActivityFragment newInstance(GraphType graphType) {
        StatisticsActivityFragment fragment = new StatisticsActivityFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_GRAPH_TYPE, graphType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            graphType = (GraphType) getArguments().getSerializable(ARG_GRAPH_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_statistics, container, false);

        Context context = getActivity();

        try {
            DatabaseHelper dbHelper = ((StatisticsActivity)getActivity()).getHelper();

            // Total words
            Dao<Word, Integer> wordDao = dbHelper.getWordDao();
            List<Word> words = wordDao.queryForAll();
            TextView totalWordsTextView = (TextView)rootView.findViewById(R.id.total_words);
            totalWordsTextView.setText(String.valueOf(words.size()));

            // Words due
            int wordsDue = 0;
            for (Word word : words) {
                if (word.isDue())
                    wordsDue++;
            }
            TextView wordsDueTextView = (TextView)rootView.findViewById(R.id.words_due);
            wordsDueTextView.setText(String.valueOf(wordsDue));

            // Words due today
            int wordsDueToday = 0;
            for (Word word : words) {
                if (word.isDueToday())
                    wordsDueToday++;
            }
            TextView wordsDueTodayTextView = (TextView)rootView.findViewById(R.id.words_due_today);
            wordsDueTodayTextView.setText(String.valueOf(wordsDueToday));

            // Words due tomorrow
            int wordsDueTomorrow = 0;
            for (Word word : words) {
                if (word.isDueTomorrow())
                    wordsDueTomorrow++;
            }
            TextView wordsDueTomorrowTextView = (TextView)rootView.findViewById(R.id.words_due_tomorrow);
            wordsDueTomorrowTextView.setText(String.valueOf(wordsDueTomorrow));

            // Words due this week
            int wordsDueThisWeek = 0;
            for (Word word : words) {
                if (word.isDueThisWeek())
                    wordsDueThisWeek++;
            }
            TextView wordsDueThisWeekTextView = (TextView)rootView.findViewById(R.id.words_due_this_week);
            wordsDueThisWeekTextView.setText(String.valueOf(wordsDueThisWeek));

            // Words due this month
            int wordsDueThisMonth = 0;
            for (Word word : words) {
                if (word.isDueThisMonth())
                    wordsDueThisMonth++;
            }
            TextView wordsDueThisMonthTextView = (TextView)rootView.findViewById(R.id.words_due_this_month);
            wordsDueThisMonthTextView.setText(String.valueOf(wordsDueThisMonth));

        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error loading statistics", Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }
}
