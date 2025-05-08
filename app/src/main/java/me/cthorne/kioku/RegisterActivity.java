package me.cthorne.kioku;

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

import me.cthorne.kioku.orm.OrmLiteBaseActivityCompat;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

import cz.msebera.android.httpclient.Header;

/**
 * Created by chris on 17/01/16.
 */
public class RegisterActivity extends OrmLiteBaseActivityCompat<DatabaseHelper> {

    public static final int MIN_PASSWORD_LENGTH = 8; // Controlled by Rails server

    private EditText emailText;
    private EditText passwordText;
    private EditText passwordConfirmationText;

    private Button registerButton;

    private View progressDim;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final Context context = this;

        emailText = (EditText) findViewById(R.id.email_text);
        passwordText = (EditText) findViewById(R.id.password_text);
        passwordConfirmationText = (EditText) findViewById(R.id.password_confirmation_text);

        if (getIntent().getExtras() != null) {
            emailText.setText(getIntent().getExtras().getString("email", ""));
            passwordText.setText(getIntent().getExtras().getString("password", ""));
        }

        passwordConfirmationText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!validateFields())
                        return true; // keep keyboard open

                    registerButton.performClick();
                }

                return false;
            }
        });

        registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateFields())
                    return;

                showLoading();

                RequestParams params = new RequestParams();
                params.put("user[email]", emailText.getText());
                params.put("user[password]", passwordText.getText());
                params.put("user[password_confirmation]", passwordConfirmationText.getText());

                KiokuServerClient serverClient = new KiokuServerClient(context);
                serverClient.postJS("/users/", params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.d("kioku-register", "we're in!");

                        hideLoading();

                        try {
                            // Save the fact user registered (thus now logged in)
                            KiokuServerClient.setLoggedIn(context, getHelper(), emailText.getText().toString(), passwordText.getText().toString(), true, new KiokuServerClient.LoginHandler() {
                                @Override
                                public void onFinish() {
                                    // Go to main activity
                                    Intent intent = new Intent(context, MainActivity.class);
                                    startActivity(intent);
                                    setResult(RESULT_OK);
                                    finish();

                                    // Welcome message
                                    Toast.makeText(context, "Welcome to Kioku!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (SQLException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Error creating local user account.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {
                        hideLoading();

                        Utils.showKeyboard(context);

                        try {
                            if (object.getJSONObject("errors") != null) {
                                if (object.getJSONObject("errors").getJSONArray("email") != null && object.getJSONObject("errors").getJSONArray("email").length() > 0) {
                                    if (object.getJSONObject("errors").getJSONArray("email").get(0).toString().equals("has already been taken")) {
                                        Toast.makeText(context, "Email address already in use.", Toast.LENGTH_SHORT).show();
                                        emailText.requestFocus();
                                        return;
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Toast.makeText(context, "Failed to register. Please try again. (" + object.toString() + ")", Toast.LENGTH_SHORT).show();
                    }
                });

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

    private boolean validateFields() {
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        String passwordConfirmation = passwordConfirmationText.getText().toString();

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.registration_invalid_email_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            Toast.makeText(this, getString(R.string.registration_password_too_short_error, MIN_PASSWORD_LENGTH), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(passwordConfirmation)) {
            Toast.makeText(this, "Password confirmation does not match password", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
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
