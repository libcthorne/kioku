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

        // Start intro activity
        Intent introIntent = new Intent(this, IntroActivity.class);
        startActivity(introIntent);
        finish();
    }
}
