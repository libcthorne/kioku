package me.cthorne.kioku;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by chris on 23/01/16.
 */
public class LoaderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loader);

        Log.d("kioku-loader", "oncreate loaderactivity");

        // Check if intro has been seen
        boolean seenIntro = KiokuServerClient.getPreferences(this).getBoolean("seenIntro", false);
        
        if (!seenIntro) {
            // Start intro activity if not seen
            Intent introIntent = new Intent(this, IntroActivity.class);
            startActivity(introIntent);
            finish();
        } else {
            // Start main activity if intro already seen
            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        }
    }
}
