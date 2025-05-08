package me.cthorne.kioku;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.SyncHttpClient;

import java.sql.SQLException;

import me.cthorne.kioku.auth.UserAccount;

/**
 * Created by chris on 17/01/16.
 */
public class KiokuServerClient {


    private static final String BASE_URL = "http://kioku.cthorne.me/";

    //"http://192.168.1.138:3000/";
    //"http://192.168.0.31:3000/";
    //"http://192.168.43.237:3000/";
    //"http://csatubu:3000/";

    public static final String PREFERENCES = "kioku_server_preferences";

    private Context context;

    public AsyncHttpClient client;
    private PersistentCookieStore cookieStore;

    public KiokuServerClient(Context context, boolean synchronous) {
        this.context = context;
        this.cookieStore = new PersistentCookieStore(context);
        this.client = synchronous ? new SyncHttpClient() : new AsyncHttpClient();
        this.client.setCookieStore(cookieStore);

        if (!isLoggedIn(context))
            this.cookieStore.clear();
    }

    public KiokuServerClient(Context context) {
        this(context, false);
    }

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public static SharedPreferences getSecretPreferences(Context context) {
        return new ObscuredSharedPreferences(context, KiokuServerClient.getPreferences(context));
    }

    public static boolean isLoggedIn(Context context) {
        return getPreferences(context).getBoolean("loggedIn", false);
    }

    public static int getCurrentUserId(Context context) {
        if (!isLoggedIn(context))
            return UserAccount.ANONYMOUS_ID;

        return getPreferences(context).getInt("currentUserId", UserAccount.ANONYMOUS_ID);
    }

    public static String getCurrentUserIdStr(Context context) {
        return String.valueOf(getCurrentUserId(context));
    }

    public static void setCurrentUserId(Context context, int userId) {
        getPreferences(context).edit().putInt("currentUserId", userId);
    }

    public static void setLoggedIn(Context context, boolean loggedIn) {
        getPreferences(context).edit().putBoolean("loggedIn", loggedIn);
    }

    public interface LoginHandler {
        void onFinish();
    }

    public static void setLoggedIn(final Context context, final DatabaseHelper dbHelper, String email, String password, boolean loggedIn, final LoginHandler handler) throws SQLException {
        getPreferences(context).edit().putBoolean("skippedLogin", false).commit(); // show login screen from now on
        Log.d("kioku-login", "set skippedLogin to false");

        setLoggedIn(context, loggedIn);
        getPreferences(context).edit().putString("email", email).putBoolean("loggedIn", loggedIn).commit();
        getSecretPreferences(context).edit().putString("password", password).commit(); // save password as encrypted

        if (loggedIn) {

            // Store current account information
            UserAccount userAccount = dbHelper.createOrFindUserAccountByEmail(email);
            getPreferences(context).edit().putInt("currentUserId", userAccount.id).commit();
            setCurrentUserId(context, userAccount.id);

            // Check for any words with no account and ask the user if they want to link them with this account
            if (dbHelper.countWordsWithNoUser() > 0) {
                new AlertDialog.Builder(context)
                        .setTitle("Unowned words detected")
                        .setMessage("Would you like to import your existing words into this account? (If you choose Cancel, you can still access your existing words later by logging out)")
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                handler.onFinish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                handler.onFinish();
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    dbHelper.convertAnonymousWordsToUserWords(getCurrentUserId(context));
                                } catch (SQLException e) {
                                    e.printStackTrace();

                                    Toast.makeText(context, "Error importing anonymous words", Toast.LENGTH_SHORT).show();
                                }

                                handler.onFinish();
                            }
                        }).create().show();
            } else {
                handler.onFinish();
            }
        } else {
            if (handler != null)
                handler.onFinish();
        }
    }

    /**
     * Adds header required for Rails to send back JSON.
     */
    public void addRailsJSHeader() {
        client.addHeader("Accept", "*/*;q=0.5, text/javascript, application/javascript, application/ecmascript, application/x-ecmascript");
    }

    public RequestHandle get(String url, RequestParams params, ResponseHandlerInterface responseHandler) {
        return client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public RequestHandle post(String url, RequestParams params, ResponseHandlerInterface responseHandler) {
        return client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public RequestHandle getJS(String url, RequestParams params, ResponseHandlerInterface responseHandler) {
        addRailsJSHeader();
        return client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public RequestHandle postJS(String url, RequestParams params, ResponseHandlerInterface responseHandler) {
        addRailsJSHeader();
        return client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

}
