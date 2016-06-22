package me.cthorne.kioku.sync;

import android.content.Context;
import android.os.Messenger;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.KiokuServerClient;

/**
 * Created by chris on 18/01/16.
 */
public class Sync {

    public static final int MESSAGE_ERROR = 0;
    public static final int MESSAGE_PROGRESS = 1;
    public static final int MESSAGE_FINISHED = 2;
    public static final int MESSAGE_FINISHED_NO_CHANGES = 3;

    public static boolean authError;

    /**
     * Sync all entities of the given name.
     * 1. Fetch index and changes from server.
     * 1a. New entities are created locally.
     * 1b. Removed entities are deleted locally.
     * 2. Push local changes to server.
     */
    public static void syncEntityType(SyncInfo syncInfo, OnEntitySyncStageFinishedHandler syncFinishedHandler) {
        // Handlers for each sync stage
        final OnEntityPushesFinishedHandler entityPushesFinishedHandler = new OnEntityPushesFinishedHandler(syncInfo, syncFinishedHandler);
        final OnMediaFileUpdatesFinishedHandler mediaFileUpdatesFinishedHandler = new OnMediaFileUpdatesFinishedHandler(syncInfo, entityPushesFinishedHandler);
        final OnEntityUpdatesFinishedHandler entityUpdatesFinishedHandler = new OnEntityUpdatesFinishedHandler(syncInfo, mediaFileUpdatesFinishedHandler);
        final OnEntityIndexHandler entityIndexHandler = new OnEntityIndexHandler(syncInfo, entityUpdatesFinishedHandler);

        // Get entity index (entityName+'s', e.g. word -> words/)
        syncInfo.getServerClient().getJS(syncInfo.getSyncItem().getEntityName() + "s/", null, entityIndexHandler);
    }

    /**
     * Syncs a list of entity items in sequential order, one after the other.
     * @param dbHelper
     * @param syncItems
     */
    public static void syncEntities(final Context context, final KiokuServerClient serverClient, final DatabaseHelper dbHelper, final ArrayList<SyncItem> syncItems, final SyncMessenger messenger, final OnEntitySyncStageFinishedHandler handler) {
        final SyncItem headItem = syncItems.get(0);

        messenger.sendProgress("Started syncing " + headItem.getEntityNameForUser());

        final AtomicBoolean failed = new AtomicBoolean();

        SyncInfo syncInfo = new SyncInfo(syncItems.get(0), context, serverClient, dbHelper, messenger, failed);
        syncEntityType(syncInfo, new OnEntitySyncStageFinishedHandler() {
            @Override
            public void onSuccess() {
                // Ignore onSuccess calls if failed
                if (failed.get())
                    return;

                messenger.sendProgress("Finished syncing " + headItem.getEntityNameForUser());

                syncItems.remove(0);

                if (syncItems.size() > 0) {
                    syncEntities(context, serverClient, dbHelper, syncItems, messenger, handler);
                } else {
                    messenger.sendFinished();
                    handler.onSuccess();
                }
            }

            @Override
            public void onFailure() {
                // Only fail once and save failure
                if (failed.getAndSet(true))
                    return;

                messenger.sendError("Error syncing " + headItem.getEntityNameForUser());
                handler.onFailure();

                serverClient.client.cancelAllRequests(true);
            }
        });
    }

    public static void startSync(final DatabaseHelper dbHelper, final Context context, Messenger messenger, OnEntitySyncStageFinishedHandler handler) {
        Log.d("kioku-sync", "start sync");

        authError = false;

        KiokuServerClient serverClient = new KiokuServerClient(context, true); // synchronous

        try {
            ArrayList<SyncItem> syncItems = new ArrayList<>();

            // Word
            syncItems.add(new SyncItem(dbHelper.getWordDao(),
                            dbHelper.qbUserWords(true, false),
                            "word"));

            // WordInformation
            syncItems.add(new SyncItem(dbHelper.getWordInformationDao(),
                                        dbHelper.qbUserWordInformations(true, false),
                                        "word_information"));

            // WordInformationTestPerformance
            syncItems.add(new SyncItem(dbHelper.getWordInformationTestPerformanceDao(),
                                        dbHelper.qbUserWordInformationTestPerformances(true, false),
                                        "word_information_test_performance"));

            // WordInformationTestAnswer
            syncItems.add(new SyncItem(dbHelper.getWordInformationTestAnswerDao(),
                    dbHelper.qbUserWordInformationTestAnswers(true, false),
                    "word_information_test_answer"));

            syncEntities(context, serverClient, dbHelper, syncItems, new SyncMessenger(messenger), handler);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("kioku-sync", "startSync sql exception");
        }
    }

}
