package me.cthorne.kioku.statistics;

import android.content.Context;

import com.github.mikephil.charting.charts.Chart;

import java.sql.SQLException;

import me.cthorne.kioku.DatabaseHelper;

/**
 * Created by chris on 30/01/16.
 */
public abstract class Graph {

    public abstract void loadThisWeek(DatabaseHelper dbHelper) throws SQLException;
    public abstract void loadThisMonth(DatabaseHelper dbHelper) throws SQLException;
    public abstract void loadThisYear(DatabaseHelper dbHelper) throws SQLException;
    public abstract void loadAllTime(DatabaseHelper dbHelper) throws SQLException;
    public abstract void clear();
    public abstract void populate();
    public abstract Chart create(Context context);
    public abstract String getTitle();

}
