package me.cthorne.kioku;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chris on 06/01/16.
 */
public class Utils {
    // Source: http://stackoverflow.com/a/9855338/5402565
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static char[] localMidnightUTC;

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    // Source: http://stackoverflow.com/a/23292787/5402565
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    public static int generateViewId() {
        if (Build.VERSION.SDK_INT < 17) {
            for (;;) {
                final int result = sNextGeneratedId.get();
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF)
                    newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }
    }

    public static File mediaDir(Context context) {
        return new File(context.getFilesDir() + File.separator + "media");
    }

    public static File mediaFile(Context context, String imageFileName) {
        // Make sure directory exists
        File mediaDir = mediaDir(context);
        mediaDir.mkdir();

        return new File(context.getFilesDir() + File.separator + "media" + File.separator + imageFileName);
    }

    /**
     * Forces the keyboard to be shown on the screen.
     * @param context
     */
    public static void showKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        Log.d("kioku-utils", "show keyboard");
    }

    /**
     * Hides the keyboard from the screen.
     * Source: http://stackoverflow.com/a/1109108/5402565
     * @param activity
     */
    public static void hideKeyboard(Activity activity) {
        // Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        // If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }

        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        Log.d("kioku-utils", "hide keyboard");
    }

    public static String saveBitmapToFile(Context context, Bitmap imageBitmap) throws IOException, NoSuchAlgorithmException {
        ByteBuffer imageBuffer = ByteBuffer.allocate(imageBitmap.getHeight() * imageBitmap.getRowBytes());
        imageBitmap.copyPixelsToBuffer(imageBuffer);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // Compress into PNG (lossless)
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        String fileName;
        try {
            // Hash file contents to get file name
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(outputStream.toByteArray());
            String digestString = Utils.bytesToHex(digest);
            fileName = digestString + ".png";
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw e;
        }

        FileOutputStream fileOutputStream = null;
        try {
            // Save compressed bitmap to file
            // Internal storage only for now
            File file = Utils.mediaFile(context, fileName);
            fileOutputStream = new FileOutputStream(file);
            outputStream.writeTo(fileOutputStream);
            outputStream.flush();
            outputStream.close();

            Log.d("kioku-js", "saved " + fileName);

            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (fileOutputStream != null)
                    fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets a Bitmap image from its URI.
     * Scales to 512x512.
     * Source: http://stackoverflow.com/a/5086706/5402565
     * @param imageUri
     * @return
     * @throws FileNotFoundException
     */
    public static Bitmap decodeUri(Context context, Uri imageUri) throws FileNotFoundException {
        //return BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 512;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri), null, o2);

        int orientation = getImageOrientation(context, imageUri);

        return rotateBitmap(bitmap, orientation);
    }

    /**
     * Rotates a bitmap by a given number of degrees.
     * Adapted from http://stackoverflow.com/a/20480741/5402565
     * @param bitmap
     * @param orientation
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        if (orientation == 0)
            return bitmap;

        Matrix matrix = new Matrix();
        matrix.setRotate(orientation);
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the orientation in degrees of an image.
     * Adapted from http://stackoverflow.com/a/20430694/5402565
     * @param context
     * @param imageUri
     * @return
     */
    public static int getImageOrientation(Context context, Uri imageUri) {
        int orientation = getOrientationFromExif(imageUri);
        Log.d("kioku-utils", "orientation from exif: " + orientation);
        if(orientation <= 0) {
            orientation = getOrientationFromMediaStore(context, imageUri);
            Log.d("kioku-utils", "orientation from mediaStore: " + orientation);
        }

        return orientation;
    }

    private static int getOrientationFromExif(Uri imageUri) {
        String imagePath = imageUri.getPath();

        int orientation = -1;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    orientation = 270;

                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    orientation = 180;

                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    orientation = 90;

                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                    orientation = 0;

                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            Log.e("kioku-utils", "Unable to get image exif orientation", e);
        }

        return orientation;
    }

    private static int getOrientationFromMediaStore(Context context, Uri imageUri) {
        String[] projection = {MediaStore.Images.ImageColumns.ORIENTATION};
        Cursor cursor = context.getContentResolver().query(imageUri, projection, null, null, null);

        int orientation = -1;
        if (cursor != null && cursor.moveToFirst()) {
            orientation = cursor.getInt(0);
            cursor.close();
        }

        return orientation;
    }

    public static Date parseRailsDateTime(String str) {
        return DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss z")
                .withZone(DateTimeZone.UTC)
                .parseDateTime(str)
                .toDate();
    }

    /**
     * Offsets the midnight timestamp according to timezone.
     * Due dates for tests are always set to UTC00:00, but the user
     * should actually see it at their timezone's 00:00. For this reason,
     * we add the offset of the user's local timezone.
     * @return
     */
    public static long getLocalMidnightUTC() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        return cal.getTimeInMillis() + cal.getTimeZone().getOffset(cal.getTimeInMillis());
    }
}
