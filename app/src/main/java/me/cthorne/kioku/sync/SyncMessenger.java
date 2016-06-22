package me.cthorne.kioku.sync;

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * Created by chris on 14/02/16.
 */
public class SyncMessenger {

    private Messenger messenger;

    public SyncMessenger(Messenger messenger) {
        this.messenger = messenger;
    }

    public void sendProgress(String msg) {
        Log.d("kioku-sync", "[progress] " + msg);

        try {
            messenger.send(Message.obtain(null, Sync.MESSAGE_PROGRESS, msg));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void sendFinished() {
        try {
            messenger.send(Message.obtain(null, Sync.MESSAGE_FINISHED));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void sendError(String msg) {
        try {
            messenger.send(Message.obtain(null, Sync.MESSAGE_ERROR, msg));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
