package me.cthorne.kioku;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivityCompat;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.sql.SQLException;

import cz.msebera.android.httpclient.Header;

/**
 * Created by chris on 17/01/16.
 */
public class LoginActivity extends OrmLiteBaseActivityCompat<DatabaseHelper> {

    private static final int REGISTRATION_RESULT_ID = 10;

    public static LoginActivity activity;

    private EditText emailText;
    private EditText passwordText;

    private Button loginButton;
    private Button registerButton;
    private Button skipButton;

    private View progressDim;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LoginActivity.activity = this;

        final Context context = this;

        MainActivity.skippedLogin = false;

        emailText = (EditText) findViewById(R.id.email_text);

        passwordText = (EditText) findViewById(R.id.password_text);
        passwordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    loginButton.performClick();

                return false;
            }
        });

        loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();

                RequestParams params = new RequestParams();
                params.put("user[email]", emailText.getText());
                params.put("user[password]", passwordText.getText());

                KiokuServerClient serverClient = new KiokuServerClient(context);
                serverClient.postJS("/users/sign_in", params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.d("kioku-login", "we're in!");

                        hideLoading();

                        try {
                            // Save the fact the user logged in
                            KiokuServerClient.setLoggedIn(context, getHelper(), emailText.getText().toString(), passwordText.getText().toString(), true, new KiokuServerClient.LoginHandler() {
                                @Override
                                public void onFinish() {
                                    // Go to main activity
                                    MainActivity.loginSync = true; // sync on load
                                    Intent intent = new Intent(context, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        } catch (SQLException e) {
                            e.printStackTrace();

                            Toast.makeText(context, "Error storing local user account.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        hideLoading();

                        // Focus on password as that is the likely incorrect candidate
                        passwordText.requestFocus();
                        Utils.showKeyboard(context);

                        Toast.makeText(context, R.string.login_auth_error, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RegisterActivity.class);
                intent.putExtra("email", emailText.getText().toString());
                intent.putExtra("password", passwordText.getText().toString());
                startActivityForResult(intent, REGISTRATION_RESULT_ID);
            }
        });

        skipButton = (Button) findViewById(R.id.skip_button);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // don't show login screen by default anymore
                MainActivity.skippedLogin = true;

                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }
        });

        progressDim = findViewById(R.id.progress_dim);
        // Prevent interaction with background views while loading
        progressDim.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REGISTRATION_RESULT_ID && resultCode == Activity.RESULT_OK)
            finish();
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        progressDim.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        progressDim.setVisibility(View.GONE);
    }
}
