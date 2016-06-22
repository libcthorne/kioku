package me.cthorne.kioku.search;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by chris on 01/11/15.
 */
public class SearchWebViewClient extends WebViewClient {

    private SearchWebViewFragment mFragment;
    private Context mContext;
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private View mProgressDim;
    private RelativeLayout mSelectButton;
    private boolean mLoading;
    private String mCurrentUrl;
    //private boolean mRedirect;

    public SearchWebViewClient(SearchWebViewFragment fragment, Context context, WebView webView, ProgressBar progressBar, View progressDim, RelativeLayout selectButton) {
        mFragment = fragment;
        mContext = context;
        mWebView = webView;
        mProgressBar = progressBar;
        mProgressDim = progressDim;
        mSelectButton = selectButton;
    }

    private void startLoading() {
        mLoading = true;
        // Show progress dialog
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressDim.setVisibility(View.VISIBLE);
        mSelectButton.setVisibility(View.VISIBLE);
        mSelectButton.setAlpha(0.2f);
        mSelectButton.setClickable(false);
    }

    private void finishLoading() {
        mLoading = false;
        // Hide progress dialog
        mProgressBar.setVisibility(View.GONE);
        mProgressDim.setVisibility(View.GONE);
        mSelectButton.setAlpha(1.0f);
        mSelectButton.setClickable(true);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
        return false;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.d("kioku-web", "onPageStarted: " + url);

        mCurrentUrl = url;

        mFragment.setInitialized(false);

        startLoading();
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Log.d("kioku-web", "onPageFinished: " + url);

        finishLoading();
    }

    public void injectLoader() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                startLoading();
                injectScriptFile(mWebView, "kioku-loader.js");
            }
        });
    }

    /**
     * This method is called using the JSI which runs on the 'JavaBridge' thread.
     * post is used to run the WebView methods on the UI thread.
     */
    public void injectInitFiles() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                injectScriptFile(mWebView, "jquery-2.1.4.min.js");
                injectScriptFile(mWebView, "kioku-jquery-noconflict.js");
                injectScriptFile(mWebView, "kioku-init.js");
                injectScriptFilesForUrl(mCurrentUrl);
                injectScriptFile(mWebView, "kioku-init-complete.js");
            }
        });
    }

    public void injectScriptFilesForUrl(String url) {
        Log.d("kioku-js", url);

        Log.d("kioku-js", "select: " + mFragment.getSelectJS());
        Log.d("kioku-js", "save: " + mFragment.getSaveJS());

        if (mFragment.hasSelectJS())
            injectScriptData(mWebView, "select.js", mFragment.getSelectJS().getBytes());
        if (mFragment.hasSaveJS())
            injectScriptData(mWebView, "save.js", mFragment.getSaveJS().getBytes());
    }

    /**
     * This method is called using the JSI which runs on the 'JavaBridge' thread.
     * post is used to run the WebView methods on the UI thread.
     */
    public void postInjectInitFiles() {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            public void run() {
                finishLoading();
                mFragment.setInitialized(true);
                mFragment.toggleSelectMode();
            }
        });

        /*new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                // Hide progress dialog
                /*mProgressBar.setVisibility(View.GONE);
                mProgressDim.setVisibility(View.GONE);
                mSelectButton.setAlpha(1.0f);
                mSelectButton.setClickable(true);

                mFragment.setInitialized(true);
                mFragment.toggleSelectMode();
            }
        });*/
    }

    /**
     * Adapted from http://stackoverflow.com/a/21612566/5402565
     */
    public void injectScriptData(WebView view, String scriptName, byte[] buffer) {
        try {
            String bufferStr = new String(buffer, "UTF-8");

            // Call JSI scriptLoaded function at the end of the script
            bufferStr += ";kiokuJSI.scriptLoaded('" + scriptName + "');";

            // String-ify the script byte-array using BASE64 encoding
            String encoded = Base64.encodeToString(bufferStr.getBytes(), Base64.NO_WRAP);

            // Inject into view
            view.loadUrl("javascript:var inject = function() {" + "" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "if (parent == null) { setTimeout(inject, 100); return; };" +
                    "var script = document.createElement('script');" +
                    "script.type = 'text/javascript';" +
                    "script.innerHTML = window.atob('" + encoded + "');" + // Base64 decode
                    "parent.appendChild(script);" +
                    "};inject();");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adapted from http://stackoverflow.com/a/21612566/5402565
     */
    public void injectScriptFile(WebView view, final String scriptFile) {
        Log.d("kioku-js", "injecting " + scriptFile);

        InputStream input = null;
        try {
            input = mContext.getAssets().open(scriptFile);
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            input.close();

            injectScriptData(view, scriptFile, buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null)
                    input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isLoading() {
        return mProgressBar.getVisibility() == View.VISIBLE;
        //return mLoading;
    }
}
