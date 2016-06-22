package me.cthorne.kioku.sync;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by chris on 14/02/16.
 */
public abstract class KiokuJsonHttpResponseHandler extends JsonHttpResponseHandler {

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
        onFailure(statusCode, headers, response != null ? response.toString() : null, throwable);
    }

}
