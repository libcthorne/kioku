package me.cthorne.kioku.statistics;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.Chart;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;

import me.cthorne.kioku.R;
import me.cthorne.kioku.DatabaseHelper;

/**
 * Created by chris on 29/01/16.
 */
public class StatisticsActivityFragment extends Fragment {

    private DatabaseHelper dbHelper;

    private GraphType graphType;
    private Graph graph;

    static StatisticsActivityFragment newInstance(GraphType graphType) {
        StatisticsActivityFragment f = new StatisticsActivityFragment();

        Bundle args = new Bundle();
        args.putInt("graphType", graphType.ordinal());
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = OpenHelperManager.getHelper(getContext(), DatabaseHelper.class);

        if (getArguments() != null) {
            graphType = GraphType.values()[getArguments().getInt("graphType")];

            switch (graphType) {
                case WORD_COUNT:
                    graph = new WordCountGraph();
                    break;
                case PERFORMANCE:
                    graph = new PerformanceGraph();
                    break;
                case STUDY_TIME:
                    graph = new StudyTimeGraph();
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        OpenHelperManager.release();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_statistics_fragment, container, false);
        RelativeLayout layoutContainer = (RelativeLayout)v.findViewById(R.id.graph_container);

        // Populate period spinner
        // http://developer.android.com/intl/en/guide/topics/ui/controls/spinner.html

        Spinner spinner = (Spinner)v.findViewById(R.id.period_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.statistics_periods_array, R.layout.statistics_period_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("kioku-stats", "selected pos " + position + "(" + adapter.getItem(position) + ")");

                try {
                    graph.clear();

                    switch (position) {
                        case 0: // Week
                            graph.loadThisWeek(dbHelper);
                            break;
                        case 1: // Month
                            graph.loadThisMonth(dbHelper);
                            break;
                        case 2: // Year
                            graph.loadThisYear(dbHelper);
                            break;
                        case 3: // All time
                            graph.loadAllTime(dbHelper);
                            break;
                    }

                    graph.populate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Select "All time" by default for performance (slight hack)
        if (graphType == GraphType.PERFORMANCE) {
            spinner.setSelection(3);
        }

        // Create graph

        Chart chart = graph.create(getContext());
        chart.setTouchEnabled(false);
        chart.setDescription("");

        // Add graph to layout

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutContainer.addView(chart, layoutParams);


        return v;
    }

}
