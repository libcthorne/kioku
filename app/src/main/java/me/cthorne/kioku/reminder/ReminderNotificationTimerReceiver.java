package me.cthorne.kioku.reminder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import me.cthorne.kioku.R;
import me.cthorne.kioku.StudyActivity;

/**
 * Created by chris on 29/11/15.
 */
public class ReminderNotificationTimerReceiver extends BroadcastReceiver {

    private int NOTIFICATION_ID = 1000;

    @Override
    public void onReceive(final Context context, Intent intent) {
        // Show notification
        Thread thread = new Thread() {
            @Override
            public void run() {
                /*DatabaseHelper dbHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

                try {
                    long remainingCount = dbHelper.countTests();

                    if (remainingCount > 0) {
                        showNotification(context);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    dbHelper.close();
                }*/
            }
        };
        thread.start();

        // Reset timer
        Intent reminderNotificationTimerServiceIntent = new Intent(context, ReminderNotificationTimerService.class);
        reminderNotificationTimerServiceIntent.putExtra("setTimer", true);
        context.startService(reminderNotificationTimerServiceIntent);
    }

    private void showNotification(Context context) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Kioku")
                        .setContentText("It's time to study!")
                        .setLights(Color.YELLOW, 500, 500)
                        .setVibrate(new long[] {0, 500, 100, 500, 100, 100, 100, 100, 100, 100, 100, 100})
                        .setAutoCancel(true); // remove notification on click

        Intent resultIntent = new Intent(context, StudyActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(StudyActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        
        // Use FLAG_IMMUTABLE for Android 12+ compatibility
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        flags
                );
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
