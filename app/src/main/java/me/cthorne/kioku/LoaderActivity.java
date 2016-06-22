package me.cthorne.kioku;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by chris on 07/11/15.
 */
public class LoaderActivity extends AppCompatActivity {

    public KiokuServerClient kiokuServerClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loader);

        kiokuServerClient = new KiokuServerClient(this);

        Intent intent;

        if (!kiokuServerClient.getPreferences(this).getBoolean("seenIntro", false))
            intent = new Intent(this, IntroActivity.class);
        else if (kiokuServerClient.isLoggedIn(this) || kiokuServerClient.getPreferences(this).getBoolean("skippedLogin", false))
            intent = new Intent(this, MainActivity.class);
        else
            intent = new Intent(this, LoginActivity.class);

        startActivity(intent);
        finish();
    }

}
