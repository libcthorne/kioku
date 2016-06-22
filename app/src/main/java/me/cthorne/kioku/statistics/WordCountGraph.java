package me.cthorne.kioku.statistics;

import android.content.Context;
import android.util.Log;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.j256.ormlite.stmt.QueryBuilder;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.words.Word;

/**
 * Created by chris on 30/01/16.
 */
public class WordCountGraph extends Graph {

    final private String[] WEEK_X_VALS = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    final private String[] MONTH_X_VALS = new String[]{"Week 1", "Week 2", "Week 3", "Week 4"};
    final private String[] YEAR_X_VALS = new String[]{"J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D"};

    private LineChart wordCountChart;

    private String[] xVals;
    Map<Integer, Long> userWordsCount;

    private long minCount;
    private long maxCount;

    private long axisMin;
    private long axisMax;

    @Override
    public void loadThisWeek(DatabaseHelper dbHelper) throws SQLException {
        xVals = WEEK_X_VALS;

        DateTime today = new DateTime();
        int todayIndex = today.getDayOfWeek()-1; // Monday=0, ..., Sunday=6

        Log.d("kioku-stats", "today=" + todayIndex);

        // Get counts for days up until today
        for (int i = 0; i <= todayIndex; i++) {
            QueryBuilder wordQb = dbHelper.getWordDao().queryBuilder();
            // use start of next day for checking what words were made within specified day
            Date date = new DateTime().minusDays(todayIndex-i-1).withTimeAtStartOfDay().toDate();
            long count = dbHelper.whereUserWords(wordQb).and().le("createdAt", date).countOf();

            putCount(i, count);
        }

        // Sample
        /*
        putCount(0, 11);
        putCount(1, 11);
        putCount(2, 13);
        putCount(3, 16);
        putCount(4, 16);
        putCount(5, 23);
        */

        updateAxisMinMax();
    }

    @Override
    public void loadThisMonth(DatabaseHelper dbHelper) throws SQLException {
        xVals = MONTH_X_VALS;

        DateTime today = new DateTime();

        // Get counts for weeks up until today
        // Week 5 is counted in week 4
        int currentWeek = Math.min((today.getDayOfMonth()-1)/7+1, 4);
        for (int week = 1; week <= currentWeek; week++) {
            QueryBuilder wordQb = dbHelper.getWordDao().queryBuilder();


            // Get start of next week to use with less than comparison
            int day = week*7+1; // last day of week + 1
            DateTime nextWeekStartDay = week == 4 ?
                    new DateTime().plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay() // first day of next month for week 4
                    : new DateTime().withDayOfMonth(day).withTimeAtStartOfDay();

            long count = dbHelper.whereUserWords(wordQb).and().lt("createdAt", nextWeekStartDay.toDate()).countOf();

            putCount(week-1, count);
        }

        updateAxisMinMax();
    }

    @Override
    public void loadThisYear(DatabaseHelper dbHelper) throws SQLException {
        xVals = YEAR_X_VALS;

        DateTime today = new DateTime();

        // Get counts for months up until current month
        for (int month = 1; month <= today.getMonthOfYear(); month++) {
            QueryBuilder wordQb = dbHelper.getWordDao().queryBuilder();

            // Get start of next month to use with less than comparison
            DateTime nextMonthStartDay = new DateTime().withMonthOfYear(month).plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay();

            long count = dbHelper.whereUserWords(wordQb).and().lt("createdAt", nextMonthStartDay.toDate()).countOf();

            putCount(month-1, count);
        }

        updateAxisMinMax();
    }

    @Override
    public void loadAllTime(DatabaseHelper dbHelper) throws SQLException {
        ArrayList<String> xValsArr = new ArrayList<>();

        Word firstCreatedWord = dbHelper.qbUserWords().orderBy("createdAt", true).queryForFirst();

        DateTime firstCreatedMonth = new DateTime(firstCreatedWord.createdAt).withDayOfMonth(1).withTimeAtStartOfDay();
        DateTime nextMonthFromNow = new DateTime().plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay();

        DateTimeFormatter f = DateTimeFormat.forPattern("MM/YY");

        // Get counts for first created month to current month
        int monthIndex = 0;
        DateTime monthStart = firstCreatedMonth;
        while (monthStart.isBefore(nextMonthFromNow)) {
            // Add month/year to X axis
            xValsArr.add(f.print(monthStart));

            // Get start of next month for less than comparison
            DateTime nextMonthStart = monthStart.plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay();

            // Get count from db
            QueryBuilder wordQb = dbHelper.getWordDao().queryBuilder();
            long count = dbHelper.whereUserWords(wordQb).and().lt("createdAt", nextMonthStart.toDate()).countOf();

            putCount(monthIndex++, count);

            // Proceed to next month
            monthStart = nextMonthStart;
        }

        // Copy x values to array
        xVals = new String[xValsArr.size()];
        xValsArr.toArray(xVals);

        updateAxisMinMax();
    }

    @Override
    public void clear() {
        userWordsCount = new HashMap<>();
        minCount = 0;
        maxCount = 0;
        axisMin = 0;
        axisMax = 0;
    }

    @Override
    public void populate() {
        YAxis yAxisLeft = wordCountChart.getAxisLeft();

        // These need to be multiples of 10 for even grid lines
        yAxisLeft.setAxisMinValue(axisMin);
        yAxisLeft.setAxisMaxValue(axisMax);

        // Data

        ArrayList<Entry> entries = new ArrayList<>();

        for (int i = 0; i < userWordsCount.size(); i++) {
            // Don't graph 0, except for the last value
            if (userWordsCount.get(i) == 0 && i != (userWordsCount.size()-1))
                continue;

            entries.add(new Entry(userWordsCount.get(i), i));
        }

        LineDataSet set = new LineDataSet(entries, "Word count");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setCircleRadius(8f);
        set.setValueTextSize(14.0f);
        //set.setDrawCubic(true);
        set.setValueFormatter(new ValueFormatter() { // display values as ints (not floats)
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "" + (int)value;
            }
        });
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);

        // X-axis values

        LineData data = new LineData(xVals, dataSets);
        wordCountChart.setData(data);
        wordCountChart.notifyDataSetChanged();
        wordCountChart.invalidate();

        wordCountChart.setPadding(0, 20, 0, 0);
    }

    @Override
    public Chart create(Context context) {
        // Line chart

        wordCountChart = new LineChart(context);
        wordCountChart.setDragEnabled(false);
        wordCountChart.setPinchZoom(false);

        // Legend settings

        Legend legend = wordCountChart.getLegend();
        legend.setEnabled(false);

        // Axis settings

        XAxis xAxis = wordCountChart.getXAxis();

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelsToSkip(0); // Prevent skipping of labels
        // Spacing between labels and axis line
        xAxis.setYOffset(15.0f);

        YAxis yAxisLeft = wordCountChart.getAxisLeft();
        yAxisLeft.setDrawLabels(false);
        yAxisLeft.setStartAtZero(false);

        YAxis yAxisRight = wordCountChart.getAxisRight();
        yAxisRight.setDrawLabels(false);
        yAxisRight.setDrawGridLines(false);

        return wordCountChart;
    }

    @Override
    public String getTitle() {
        return "Word Count";
    }

    private void putCount(int i, long count) {
        userWordsCount.put(i, count);

        if (minCount == 0 || count < minCount) // don't use 0 as min if possible
            minCount = count;

        if (count > maxCount)
            maxCount = count;
    }

    private void updateAxisMinMax() {
        Log.d("kioku-stats", minCount + "," + maxCount);

        // min-1 rounded down to nearest 10
        // makes sure the min is never touching the axis
        axisMin = minCount-1;
        axisMin -= axisMin%10;

        /*
        // 40 more than max (if min/max diff >= 40)
        // 40 more than min (if min/max diff < 40)
        // rounded down to nearest 40
        axisMax = (maxCount-minCount>=40) ? maxCount+40 : minCount+40;
        Log.d("kioku-stats", "m1=" + axisMax);
        axisMax -= axisMax%40;
        Log.d("kioku-stats", "m2=" + axisMax);
        */

        // 40 more than max
        // rounded down to nearest 40
        axisMax = maxCount+40;
        axisMax -= axisMax%40;
    }
}
