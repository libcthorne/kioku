package me.cthorne.kioku.search;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.regex.Pattern;

import me.cthorne.kioku.SearchResultsActivity;
import me.cthorne.kioku.Utils;
import me.cthorne.kioku.words.WordInformation;
import me.cthorne.kioku.words.WordInformationType;

/**
 * Created by chris on 01/11/15.
 */
public class SearchJSI {
    private int fragmentCount;
    public static int savedCount = 0;

    private SearchResultsActivity activity;
    private SearchWebViewClient webViewClient;

    public SearchJSI(SearchResultsActivity activity, SearchWebViewClient webViewClient) {
        this.activity = activity;
        this.webViewClient = webViewClient;
        this.fragmentCount = activity.mAdapter.getSavedWebViewFragments().size();
    }

    @JavascriptInterface
    public void loaded() {
        Log.d("kioku-js", "loaded");

        webViewClient.injectInitFiles();
    }

    @JavascriptInterface
    public void retryLoad() {
        Log.d("kioku-js", "retry load");

        webViewClient.injectLoader();
    }

    @JavascriptInterface
    public void initComplete() {
        Log.d("kioku-js", "init complete!");
        webViewClient.postInjectInitFiles();
    }

    @JavascriptInterface
    public void saveWordInformationString(String informationType, String informationString, String metaInformationString) {
        Log.d("kioku-js", "save information string (" + informationType + "): " + informationString + "[" + metaInformationString + "]");

        // Create word information object
        WordInformation wordInformation;
        if (informationString != null) {
            if (metaInformationString != null) {
                // Info + meta
                Log.d("kioku-js", "saving info+meta");
                wordInformation = new WordInformation(informationType, informationString.getBytes(), metaInformationString.getBytes());
            } else {
                // Info only
                Log.d("kioku-js", "saving info only");
                wordInformation = new WordInformation(informationType, informationString.getBytes());
            }
        } else {
            // Null + meta
            Log.d("kioku-js", "saving null+meta");
            wordInformation = new WordInformation(informationType, null, metaInformationString.getBytes());
        }

        // Store word information object to save in the database
        activity.wordInformations.add(wordInformation);
    }

    @JavascriptInterface
    public void saveWordInformationString(String informationType, String informationString) {
        saveWordInformationString(informationType, informationString, null);
    }

    @JavascriptInterface
    public void saveWordInformationImage(String imageUrl) {
        Log.d("kioku-js", "save information image: " + imageUrl);

        Bitmap imageBitmap = null;

        if (Pattern.compile("^data:image/.+;base64,").matcher(imageUrl).find()) {
            // From base64

            imageUrl = imageUrl.substring(imageUrl.indexOf(",") + 1); // Don't include data:image/... for decoding
            byte[] image = Base64.decode(imageUrl, Base64.DEFAULT);
            imageBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);

            if (imageBitmap == null) {
                Log.e("kioku-js", "couldn't get image bitmap for " + imageUrl);
                return;
            }

            try {
                String fileName = Utils.saveBitmapToFile(activity, imageBitmap);
                // Create word information object
                WordInformation wordInformation = new WordInformation(WordInformationType.IMAGE, fileName.getBytes());
                // Store word information object to save in the database
                activity.wordInformations.add(wordInformation);
            } catch (Exception e) {
                Log.d("kioku-js", "error saving image for " + imageUrl + ": " + e.getMessage());
            }
        } else {
            // Asynchronous download from URL

            activity.startDownload();

            Picasso.with(activity)
                    .load(imageUrl)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                            activity.finishDownload(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            activity.finishDownload(null);
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });
        }
    }

    @JavascriptInterface
    public void saveFinished() {
        Log.d("kioku-js", "saved finished (" + (savedCount + 1) + "/" + fragmentCount + ")");

        if (++savedCount == fragmentCount)
            activity.saveWord();
    }

    @JavascriptInterface
    public void log(String message) {
        Log.d("kioku-js", "log: " + message);
    }

    @JavascriptInterface
    public boolean getSelectedMode() {
        return false;
        //return activity.mSelectMode;
    }

    @JavascriptInterface
    public void scriptLoaded(String scriptName) {
        Log.d("kioku-js", "loaded: " + scriptName);
    }


    @JavascriptInterface
    public void selectedElement() {
        // Run in UI thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (activity.secondHint.getVisibility() == View.VISIBLE) {
                    activity.secondHint.setVisibility(View.GONE);
                    activity.thirdHint.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @JavascriptInterface
    public void deselectedElement() {

    }
}
