package me.cthorne.kioku.orm;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import me.cthorne.kioku.DatabaseHelper;

/**
 * Base class to use for activities in Android.
 *
 * You can simply call {@link #getHelper()} to get your helper class, or {@link #getConnectionSource()} to get a
 * {@link ConnectionSource}.
 *
 * The method {@link #getHelper()} assumes you are using the default helper factory -- see {@link OpenHelperManager}. If
 * not, you'll need to provide your own helper instances which will need to implement a reference counting scheme. This
 * method will only be called if you use the database, and only called once for this activity's life-cycle. 'close' will
 * also be called once for each call to createInstance.
 *
 * @author graywatson, kevingalligan
 */
public abstract class OrmLiteBaseActivityCompat<H extends OrmLiteSqliteOpenHelper> extends AppCompatActivity {

    private volatile H helper;
    private volatile boolean created = false;
    private volatile boolean destroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (helper == null) {
            helper = getHelperInternal(this);
            created = true;
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseHelper(helper);
        destroyed = true;
    }

    /**
     * Get a helper for this action.
     */
    public H getHelper() {
        if (helper == null) {
            if (!created) {
                helper = getHelperInternal(this);
                created = true;
            } else if (destroyed) {
                throw new IllegalStateException("A call to onDestroy has already been made and the helper cannot be used after that point");
            } else {
                throw new IllegalStateException("Helper is null for some unknown reason");
            }
        } else {
            if (destroyed) {
                throw new IllegalStateException("A call to onDestroy has already been made and the helper cannot be used after that point");
            }
        }
        return helper;
    }

    /**
     * Get a connection source for this action.
     */
    public ConnectionSource getConnectionSource() {
        return getHelper().getConnectionSource();
    }

    /**
     * This is called internally by the class to create the helper early.  If you need to override this to use a custom
     * helper, see {@link #getHelperInternal(Context)} for the preferred way to do that.
     */
    protected void createHelper(Context context, Class<? extends OrmLiteSqliteOpenHelper> openHelperClass) {
        helper = getHelperInternal(context);
        created = true;
    }

    /**
     * This is called internally by the class to create the helper early.  If you need to create the helper
     * yourself, you should override this method to avoid calling {@link #getHelper()} in your Activity's
     * {@link #onCreate(Bundle)} method which can cause problems with the Android lifecycle.
     */
    protected H getHelperInternal(Context context) {
        @SuppressWarnings("unchecked")
        H newHelper = (H) OpenHelperManager.getHelper(context, DatabaseHelper.class);
        return newHelper;
    }

    /**
     * Release the helper instance created in {@link #getHelper()}.
     */
    protected void releaseHelper(H helper) {
        OpenHelperManager.releaseHelper();
        this.helper = null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(super.hashCode());
    }
}
