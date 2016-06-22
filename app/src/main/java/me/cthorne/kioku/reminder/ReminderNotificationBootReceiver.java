package me.cthorne.kioku.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by chris on 29/11/15.
 */
public class ReminderNotificationBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            Intent reminderNotificationServiceIntent = new Intent(context, ReminderNotificationTimerService.class);
            context.startService(reminderNotificationServiceIntent);
        }
    }
}