package me.cthorne.kioku.sync;

import android.content.Context;

import java.util.concurrent.atomic.AtomicBoolean;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.KiokuServerClient;

/**
 * Created by chris on 30/01/16.
 */
public class SyncInfo {
    private SyncItem syncItem;
    private Context context;
    private KiokuServerClient serverClient;
    private DatabaseHelper dbHelper;
    private SyncMessenger messenger;
    private AtomicBoolean failed;

    public SyncInfo(SyncItem syncItem, Context context, KiokuServerClient serverClient, DatabaseHelper dbHelper, SyncMessenger messenger, AtomicBoolean failed) {
        this.syncItem = syncItem;
        this.context = context;
        this.serverClient = serverClient;
        this.dbHelper = dbHelper;
        this.messenger = messenger;
        this.failed = failed;
    }

    public SyncItem getSyncItem() {
        return syncItem;
    }

    public Context getContext() {
        return context;
    }

    public KiokuServerClient getServerClient() {
        return serverClient;
    }

    public DatabaseHelper getDbHelper() {
        return dbHelper;
    }

    public SyncMessenger getMessenger() { return messenger; }

    public AtomicBoolean getFailed() {
        return failed;
    }
}