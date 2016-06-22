package me.cthorne.kioku.sync;

import android.app.IntentService;
import android.content.Intent;
import android.os.Messenger;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import me.cthorne.kioku.DatabaseHelper;

/**
 * Created by chris on 14/02/16.
 */
//public class SyncIntentService extends OrmLiteBaseIntentService<DatabaseHelper> {
public class SyncIntentService extends IntentService {

    public SyncIntentService() {
        super("SyncIntentService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SyncIntentService(String name) {
        super(name);
    }

    protected void onHandleIntent(Intent intent) {
        final DatabaseHelper dbHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);

        Messenger messenger = (Messenger)intent.getExtras().get("messenger");
        Sync.startSync(dbHelper, this, messenger, new OnEntitySyncStageFinishedHandler() {

            @Override
            public void onSuccess() {
                OpenHelperManager.releaseHelper();
            }

            @Override
            public void onFailure() {
                OpenHelperManager.releaseHelper();
            }
        });
    }
}
