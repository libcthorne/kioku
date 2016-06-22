package me.cthorne.kioku.statistics;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.test.WordInformationTestType;
import me.cthorne.kioku.test.tests.Tests;

/**
 * Created by chris on 30/01/16.
 */
public class PerformanceGraph extends Graph {

    /*final private static WordInformationTestType testTypes[] = new WordInformationTestType[]{
            WordInformationTestType.VOCABULARY_RECALL,
            WordInformationTestType.VOCABULARY_COMPREHENSION,
            WordInformationTestType.SENTENCE_COMPREHENSION,
            WordInformationTestType.KANJI_WRITING,
            WordInformationTestType.KANJI_READING
    };*/

    private RadarChart performanceChart;
    private Set<WordInformationTestType> testTypes;
    private HashMap<WordInformationTestType, Long> forgotCounts;
    private HashMap<WordInformationTestType, Long> rememberedCounts;
    private long minForgot;
    private long maxForgot;
    private long minRemembered;
    private long maxRemembered;

    @Override
    public void clear() {
        forgotCounts = new HashMap<>();
        rememberedCounts = new HashMap<>();
        minForgot = 0;
        maxForgot = 0;
        minRemembered = 0;
        maxRemembered = 0;
    }

    @Override
    public void loadThisWeek(DatabaseHelper dbHelper) throws SQLException {
        for (WordInformationTestType testType : testTypes) {
            long forgotCount = dbHelper.qbUserTestAnswersThisWeek(testType, 0).countOf();
            long rememberedCount = dbHelper.qbUserTestAnswersThisWeek(testType, 1).countOf();
            putCounts(testType, forgotCount, rememberedCount);
        }
    }

    @Override
    public void loadThisMonth(DatabaseHelper dbHelper) throws SQLException {
        for (WordInformationTestType testType : testTypes) {
            long forgotCount = dbHelper.qbUserTestAnswersThisMonth(testType, 0).countOf();
            long rememberedCount = dbHelper.qbUserTestAnswersThisMonth(testType, 1).countOf();
            putCounts(testType, forgotCount, rememberedCount);
        }
    }

    @Override
    public void loadThisYear(DatabaseHelper dbHelper) throws SQLException {
        for (WordInformationTestType testType : testTypes) {
            long forgotCount = dbHelper.qbUserTestAnswersThisYear(testType, 0).countOf();
            long rememberedCount = dbHelper.qbUserTestAnswersThisYear(testType, 1).countOf();
            putCounts(testType, forgotCount, rememberedCount);
        }
    }

    @Override
    public void loadAllTime(DatabaseHelper dbHelper) throws SQLException {
        for (WordInformationTestType testType : testTypes) {
            long forgotCount = dbHelper.qbUserTestAnswers(testType, 0).countOf();
            long rememberedCount = dbHelper.qbUserTestAnswers(testType, 1).countOf();
            putCounts(testType, forgotCount, rememberedCount);
        }
    }

    @Override
    public void populate() {
        // Data

        ArrayList<Entry> rememberedEntries = new ArrayList<>();
        //ArrayList<Entry> forgotEntries = new ArrayList<>();

        int i = 0;
        for (WordInformationTestType testType : testTypes) {
            //Long forgotCount = forgotCounts.get(testType);
            Long rememberedCount = rememberedCounts.get(testType);

            //Log.d("kioku-stats", "test type " + testType + " f" + forgotCount + "r" + rememberedCount);
            Log.d("kioku-stats", "test type " + testType + " r" + rememberedCount);

            /*if (maxForgot == 0 || forgotCount == null)
                forgotEntries.add(new Entry(0.1f, i));
            else
                forgotEntries.add(new Entry(forgotCount, i));*/

            //if (rememberedCount == 0)
            //    rememberedEntries.add(new Entry(0.1f, i));
            //else
                rememberedEntries.add(new Entry(rememberedCount, i++));
        }

        /*RadarDataSet forgotSet = new RadarDataSet(forgotEntries, "Forgot");
        forgotSet.setDrawFilled(true);
        forgotSet.setDrawValues(false);
        forgotSet.setColor(Color.YELLOW);
        forgotSet.setLineWidth(1f);
        if (maxForgot == 0)
            forgotSet.setVisible(false);*/

        RadarDataSet rememberedSet = new RadarDataSet(rememberedEntries, "Remembered");
        rememberedSet.setDrawFilled(true);
        rememberedSet.setDrawValues(false);
        rememberedSet.setColor(Color.parseColor("#02C01F"));
        rememberedSet.setLineWidth(2f);
        if (maxRemembered == 0)
           rememberedSet.setVisible(false);

        ArrayList<IRadarDataSet> dataSets = new ArrayList<>();
        dataSets.add(rememberedSet);
        //dataSets.add(forgotSet);

        ArrayList<String> xVals = new ArrayList<>();
        for (WordInformationTestType testType : testTypes) {
            xVals.add(testType.toName());
        }

        RadarData data = new RadarData(xVals, dataSets);

        performanceChart.getYAxis().setAxisMaxValue(120.0f);
        performanceChart.setData(data);
        performanceChart.notifyDataSetChanged();
        performanceChart.invalidate();
    }

    @Override
    public Chart create(Context context) {
        // Get test types
        testTypes = new TreeSet<>(Tests.getAll().keySet());

        // Create chart

        performanceChart = new RadarChart(context);

        // Axes

        XAxis xAxis = performanceChart.getXAxis();
        xAxis.setTextSize(6.0f);

        YAxis yAxis = performanceChart.getYAxis();
        yAxis.setDrawLabels(false);

        // Legend

        //performanceChart.getLegend().setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        performanceChart.getLegend().setEnabled(false);

        return performanceChart;
    }

    @Override
    public String getTitle() {
        return "Performance";
    }

    private void putCounts(WordInformationTestType testType, long forgotCount, long rememberedCount) {
        Log.d("kioku-stats", "forgot " + forgotCount + ", remembered " + rememberedCount);
        long newRememberedCount = (long)((rememberedCount/((float)rememberedCount+forgotCount*3l))*100);
        Log.d("kioku-stats", "=> " + newRememberedCount);

        // Put into map
        //forgotCounts.put(testType, forgotCount);
        //rememberedCounts.put(testType, rememberedCount);
        rememberedCounts.put(testType, newRememberedCount);


        // Update min max
        //maxForgot = Math.max(maxForgot, forgotCount);
        maxRemembered = Math.max(maxRemembered, rememberedCount);

        /*if (minForgot == 0 || forgotCount < minForgot)
            minForgot = forgotCount;
        if (minRemembered == 0 || rememberedCount < minRemembered)
            minRemembered = rememberedCount;*/
    }


}