package me.cthorne.kioku.search;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import me.cthorne.kioku.R;
import me.cthorne.kioku.SearchResultsActivity;

public class SearchWebViewFragment extends Fragment {
    private String url;
    private String selectJS;
    private String saveJS;

    private SearchResultsActivity activity;
    private SearchWebViewClient mWebViewClient;
    private View mProgressDim;
    private ProgressBar mProgressBar;
    private WebView mWebView;
    private RelativeLayout mSelectButton;
    private boolean mSelectMode;

    static SearchWebViewFragment newInstance(String url, String selectJS, String saveJS) {
        SearchWebViewFragment f = new SearchWebViewFragment();

        Bundle args = new Bundle();
        args.putString("url", url);
        args.putString("selectJS", selectJS);
        args.putString("saveJS", saveJS);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (SearchResultsActivity)getActivity();

        boolean n = getArguments() != null;
        url = n ? getArguments().getString("url") : "";
        selectJS = n ? getArguments().getString("selectJS") : "";
        saveJS = n ? getArguments().getString("saveJS") : "";

        Log.d("kioku-search", "url: " + url);
        Log.d("kioku-search", "select: " + selectJS);
        Log.d("kioku-search", "save: " + saveJS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.word_search_web_view, container, false);

        mProgressBar = (ProgressBar) v.findViewById(R.id.fragment_progress_bar);
        mProgressDim = v.findViewById(R.id.fragment_progress_dim);
        mSelectButton = (RelativeLayout)v.findViewById(R.id.search_select_button);

        mWebView = (WebView) v.findViewById(R.id.fragment_web_view);
        mWebViewClient = new SearchWebViewClient(this, getContext(), mWebView, mProgressBar, mProgressDim, mSelectButton);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new SearchJSI(activity, mWebViewClient), "kiokuJSI");
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //return mWebViewClient.isLoading();
                return false; // allow interaction even while loading
            }
        });
        mWebView.loadUrl(url);

        // Select button
        mSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSelectMode();

                if (SearchResultsActivity.activeActivity.firstHint.getVisibility() == View.VISIBLE) {
                    SearchResultsActivity.activeActivity.firstHint.setVisibility(View.GONE);
                    SearchResultsActivity.activeActivity.secondHint.setVisibility(View.VISIBLE);
                }
            }
        });

        return v;
    }

    public void setSelectMode(final boolean inSelectMode) {
        Log.d("kioku-search", "selectMode: " + inSelectMode);

        mSelectMode = inSelectMode;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (inSelectMode) {
                    mSelectButton.setScaleX(0.92f);
                    mSelectButton.setScaleY(0.92f);
                    mSelectButton.setAlpha(0.7f);
                } else {
                    mSelectButton.setScaleX(1);
                    mSelectButton.setScaleY(1);
                    mSelectButton.setAlpha(1);
                }
            }
        });
    }

    public WebView getWebView() {
        return mWebView;
    }

    public SearchWebViewClient getWebViewClient() {
        return mWebViewClient;
    }

    private boolean mInitialized;

    public void setInitialized(boolean initialized) {
        Log.d("kioku-js", "set initialized: " + initialized);
        this.mInitialized = initialized;
    }

    public void toggleSelectMode() {
        Log.d("kioku-search", "toggleSelectMode");

        if (!mInitialized) {
            mWebViewClient.injectLoader();
            return;
        }

        setSelectMode(!mSelectMode);

        mWebViewClient.injectScriptFile(mWebView, "kioku-web-toggle-select-mode.js");
    }

    public void saveSelected() {
        mWebViewClient.injectScriptFile(mWebView, "kioku-web-save.js");
    }

    public String getUrl() {
        return url;
    }

    public String getSelectJS() {
        return selectJS;
    }

    public String getSaveJS() {
        return saveJS;
    }

    public boolean hasSelectJS() {
        return selectJS.length() > 0;
    }

    public boolean hasSaveJS() {
        return saveJS.length() > 0;
    }

    public void reload() {
        Log.d("kioku-search", "reload");
        setSelectMode(false);
        setInitialized(false);
        getWebView().reload();
    }
}
