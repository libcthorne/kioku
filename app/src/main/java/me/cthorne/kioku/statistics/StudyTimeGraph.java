package me.cthorne.kioku.statistics;

import android.content.Context;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.words.Word;

/**
 * Created by chris on 30/01/16.
 */
public class StudyTimeGraph extends Graph {

    private static final float LOWEST_MAX_DAY_MINUTES = 30;
    private static final float LOWEST_MAX_WEEK_HOURS = 3;
    private static final float LOWEST_MAX_MONTH_HOURS = 8;
    private static final float LOWEST_MAX_ALL_TIME_HOURS = LOWEST_MAX_MONTH_HOURS;

    final private String[] WEEK_X_VALS = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    final private String[] MONTH_X_VALS = new String[]{"Week 1", "Week 2", "Week 3", "Week 4"};
    final private String[] YEAR_X_VALS = new String[]{"J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D"};

    private BarChart studyTimeChart;

    private String[] xVals;
    private Map<Integer, Float> studyTimes;

    private float maxTime;
    private float axisMax;

    private String timeUnitStr;

    @Override
    public void loadThisWeek(DatabaseHelper dbHelper) throws SQLException {
        timeUnitStr = "Minutes";
        xVals = WEEK_X_VALS;

        DateTime today = new DateTime();
        int todayIndex = today.getDayOfWeek()-1; // Monday=0, ..., Sunday=6

        // Get total times for days up until today
        for (int i = 0; i <= todayIndex; i++) {
            DateTime date = new DateTime().minusDays(todayIndex - i);

            float totalSeconds = dbHelper.sumAnswerSecondsForDay(date);

            putStudyTime(i, totalSeconds/60.0f); // convert to minutes
        }

        // Sample
        /*putStudyTime(0, 10);
        putStudyTime(1, 8);
        putStudyTime(2, 0);
        putStudyTime(3, 13);
        putStudyTime(4, 12);
        putStudyTime(5, 13);*/

        updateAxisMinMax(LOWEST_MAX_DAY_MINUTES);
    }

    @Override
    public void loadThisMonth(DatabaseHelper dbHelper) throws SQLException {
        timeUnitStr = "Hours";
        xVals = MONTH_X_VALS;

        DateTime today = new DateTime();

        // Get totals for weeks up until today
        // Week 5 is counted in week 4
        for (int week = 1; week <= Math.min(today.getDayOfMonth()/7+1, 4); week++) {
            int firstDayOfThisWeek = (week-1)*7+1;
            int firstDayOfNextWeek = firstDayOfThisWeek+7;

            // Start date of this week for lower bound of range
            DateTime weekStartDay = new DateTime().withDayOfMonth(firstDayOfThisWeek).withTimeAtStartOfDay();

            // Start date of next week for upper bound of range
            DateTime nextWeekStartDay = week == 4 ?
                    new DateTime().plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay() // first day of next month for week 4
                    : new DateTime().withDayOfMonth(firstDayOfNextWeek).withTimeAtStartOfDay();

            float totalSeconds = dbHelper.sumAnswerSecondsForDateRange(weekStartDay, nextWeekStartDay);

            Log.d("kioku-stats", "totalSeconds for week " + week + ": " + totalSeconds);

            putStudyTime(week-1, totalSeconds/3600.0f); // convert to hours
        }

        updateAxisMinMax(LOWEST_MAX_WEEK_HOURS);
    }

    @Override
    public void loadThisYear(DatabaseHelper dbHelper) throws SQLException {
        timeUnitStr = "Hours";
        xVals = YEAR_X_VALS;

        DateTime today = new DateTime();

        // Get times for months up until current month
        for (int month = 1; month <= today.getMonthOfYear(); month++) {
            // Start of this month for lower bound of range
            DateTime thisMonthStartDay = new DateTime().withMonthOfYear(month).withDayOfMonth(1).withTimeAtStartOfDay();
            // Start of next month for upper bound of range
            DateTime nextMonthStartDay = new DateTime().withMonthOfYear(month).plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay();

            float totalSeconds = dbHelper.sumAnswerSecondsForDateRange(thisMonthStartDay, nextMonthStartDay);
            putStudyTime(month-1, totalSeconds/3600.0f); // convert to hours
        }

        updateAxisMinMax(LOWEST_MAX_MONTH_HOURS);
    }

    @Override
    public void loadAllTime(DatabaseHelper dbHelper) throws SQLException {
        timeUnitStr = "Hours";

        ArrayList<String> xValsArr = new ArrayList<>();

        Word firstCreatedWord = dbHelper.qbUserWords().orderBy("createdAt", true).queryForFirst();

        DateTime firstCreatedMonth = new DateTime(firstCreatedWord.createdAt).withDayOfMonth(1).withTimeAtStartOfDay();
        DateTime nextMonthFromNow = new DateTime().plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay();

        DateTimeFormatter f = DateTimeFormat.forPattern("MM/YY");

        // Get times for first created month to current month
        int monthIndex = 0;
        DateTime monthStart = firstCreatedMonth;
        while (monthStart.isBefore(nextMonthFromNow)) {
            // Add month/year to X axis
            xValsArr.add(f.print(monthStart));

            // Get start of next month for less than comparison
            DateTime nextMonthStart = monthStart.plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay();

            float totalSeconds = dbHelper.sumAnswerSecondsForDateRange(monthStart, nextMonthStart);
            putStudyTime(monthIndex++, totalSeconds / 3600.0f); // convert to hours

            // Proceed to next month
            monthStart = nextMonthStart;
        }

        // Copy x values to array
        xVals = new String[xValsArr.size()];
        xValsArr.toArray(xVals);

        updateAxisMinMax(LOWEST_MAX_ALL_TIME_HOURS);
    }

    @Override
    public void clear() {
        studyTimes = new HashMap<>();
        maxTime = 0.0f;
        axisMax = 0;
    }

    @Override
    public void populate() {
        YAxis yAxisLeft = studyTimeChart.getAxisLeft();
        yAxisLeft.setAxisMaxValue(axisMax);

        // Data

        ArrayList<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < studyTimes.size(); i++) {
            BarEntry e = new BarEntry(studyTimes.get(i), i);
            entries.add(e);
        }

        BarDataSet set = new BarDataSet(entries, timeUnitStr + " studied");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setDrawValues(false);
        set.setValueTextSize(12f);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);

        BarData data = new BarData(xVals, dataSets);
        studyTimeChart.setData(data);
        studyTimeChart.notifyDataSetChanged();
        studyTimeChart.invalidate();
    }

    @Override
    public Chart create(Context context) {
        // Create chart

        studyTimeChart = new BarChart(context);
        studyTimeChart.setDragEnabled(false);
        studyTimeChart.setPinchZoom(false);

        // Axes

        XAxis xAxis = studyTimeChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelsToSkip(0); // Prevent skipping of labels
        // Spacing between labels and axis line
        xAxis.setYOffset(15.0f);

        YAxis yAxisRight = studyTimeChart.getAxisRight();
        yAxisRight.setDrawLabels(false);
        yAxisRight.setDrawGridLines(false);

        // Show minutes as integers, not floats
        YAxis yAxisLeft = studyTimeChart.getAxisLeft();
        yAxisLeft.setValueFormatter(new YAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, YAxis yAxis) {
                if (value - Math.floor(value) != 0.0f) // Don't show decimals
                    return "";

                return "" + (int) value;
            }
        });

        // Legend

        studyTimeChart.getLegend().setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);

        return studyTimeChart;
    }

    @Override
    public String getTitle() {
        return "Study Time";
    }

    private void putStudyTime(int i, float studyTime) {
        studyTimes.put(i, studyTime);

        maxTime = Math.max(maxTime, studyTimes.get(i));
    }

    private void updateAxisMinMax(float lowestPossibleMax) {
        // n = lowestPossibleMax
        // n more than max (if min/max diff >= n)
        // max=n (if min/max diff < n)
        // rounded down to nearest n
        axisMax = (maxTime>=lowestPossibleMax) ? maxTime+lowestPossibleMax : lowestPossibleMax;
        axisMax -= axisMax%lowestPossibleMax;
    }
}