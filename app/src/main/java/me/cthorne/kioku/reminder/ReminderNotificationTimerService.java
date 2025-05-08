package me.cthorne.kioku.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;

import java.util.Calendar;

import me.cthorne.kioku.OldSettingsActivity;

/**
 * Created by chris on 29/11/15.
 */
public class ReminderNotificationTimerService extends Service {
    private int reminderHourOfDay;
    private int reminderMinute;

    @Override
    public void onCreate() {
        SharedPreferences prefs = getSharedPreferences(OldSettingsActivity.SETTINGS_PREF_FILE, Context.MODE_PRIVATE);
        if (!prefs.getBoolean("reminderEnabled", true))
            return;

        reminderHourOfDay = prefs.getInt("reminderHourOfDay", 0);
        reminderMinute = prefs.getInt("reminderMinute", 0);

        setTimer();

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Reset the timer if started from ReminderNotificationTimerReceiver
        // which sets setTimer=true
        if (intent != null && intent.getExtras() != null) {
            if (intent.getBooleanExtra("setTimer", false))
                setTimer();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        cancelTimer();

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setTimer() {
        Intent reminderNotificationIntent = new Intent(this, ReminderNotificationTimerReceiver.class);
        
        // Use FLAG_IMMUTABLE for Android 12+ compatibility
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, reminderNotificationIntent, flags);

        Calendar calendar = Calendar.getInstance();

        if (calendar.get(Calendar.HOUR_OF_DAY) > reminderHourOfDay || (calendar.get(Calendar.HOUR_OF_DAY) == reminderHourOfDay && calendar.get(Calendar.MINUTE) >= reminderMinute))
            calendar.add(Calendar.DATE, 1); // Add one day if today is already past the reminder time

        calendar.set(Calendar.HOUR_OF_DAY, reminderHourOfDay);
        calendar.set(Calendar.MINUTE, reminderMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Create new alarm
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
    }

    private void cancelTimer() {
        Intent reminderNotificationIntent = new Intent(this, ReminderNotificationTimerReceiver.class);
        
        // Use FLAG_IMMUTABLE for Android 12+ compatibility
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, reminderNotificationIntent, flags);

        // Cancel alarm
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(alarmIntent);
    }

}
