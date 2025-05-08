package me.cthorne.kioku;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import me.cthorne.kioku.reminder.ReminderNotificationTimerService;

/**
 * Created by chris on 30/11/15.
 */
public class OldSettingsActivity extends AppCompatActivity {

    public final static String SETTINGS_PREF_FILE = "kioku-settings";

    static TextView reminderTimeText;
    Button setReminderTimeButton;
    CheckBox reminderEnabledCheckBox;

    private static Context context;

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            SharedPreferences prefs = context.getSharedPreferences(SETTINGS_PREF_FILE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("reminderHourOfDay", hourOfDay);
            editor.putInt("reminderMinute", minute);
            editor.apply();

            reminderTimeText.setText("Reminder time: " + getReminderTimeString());

            if (prefs.getBoolean("reminderEnabled", true)) {
                // Restart reminder service to use new time if enabled
                Intent reminderNotificationServiceIntent = new Intent(context, ReminderNotificationTimerService.class);
                context.stopService(reminderNotificationServiceIntent);
                context.startService(reminderNotificationServiceIntent);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences prefs = context.getSharedPreferences(SETTINGS_PREF_FILE, Context.MODE_PRIVATE);

        reminderTimeText = (TextView) findViewById(R.id.reminder_time_text);
        reminderTimeText.setText("Reminder time: " + getReminderTimeString());

        setReminderTimeButton = (Button) findViewById(R.id.set_reminder_time_button);
        setReminderTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });

        reminderEnabledCheckBox = (CheckBox) findViewById(R.id.reminder_enabled_checkbox);
        reminderEnabledCheckBox.setChecked(prefs.getBoolean("reminderEnabled", true));
        reminderEnabledCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = context.getSharedPreferences(SETTINGS_PREF_FILE, Context.MODE_PRIVATE).edit();
                editor.putBoolean("reminderEnabled", isChecked);
                editor.apply();

                Intent reminderNotificationServiceIntent = new Intent(context, ReminderNotificationTimerService.class);
                if (isChecked)
                    context.startService(reminderNotificationServiceIntent);
                else
                    context.stopService(reminderNotificationServiceIntent);
            }
        });
    }

    private static String getReminderTimeString() {
        SharedPreferences prefs = context.getSharedPreferences(SETTINGS_PREF_FILE, Context.MODE_PRIVATE);
        int hourOfDay = prefs.getInt("reminderHourOfDay", -1);
        int minute = prefs.getInt("reminderMinute", -1);

        return (hourOfDay<10?"0"+hourOfDay:hourOfDay) + ":" + (minute<10?"0"+minute:minute);
    }

}
