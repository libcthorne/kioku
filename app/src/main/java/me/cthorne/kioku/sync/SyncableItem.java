package me.cthorne.kioku.sync;

import android.content.Context;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.sql.SQLException;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.KiokuServerClient;
import me.cthorne.kioku.test.WordInformationTestAnswer;
import me.cthorne.kioku.test.WordInformationTestPerformance;
import me.cthorne.kioku.words.Word;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 19/01/16.
 */
public abstract class SyncableItem {
    // NOTE: if inserting a value before NEW, update the defaultValue of syncState accordingly
    public enum SyncState {
        SYNCED, // no changes since last sync
        NEW, // waiting to be synced
        UPDATED, // changed since last sync
        DELETED // deleted since last sync
    }

    // Local id
    @DatabaseField(generatedId = true)
    public int id;

    // Sync information
    @DatabaseField
    private Integer syncId;
    @DatabaseField
    private Integer syncVersion;
    @DatabaseField(dataType = DataType.ENUM_INTEGER, defaultValue = "1") // Default value: ordinal value of SyncState.NEW
    private SyncState syncState;

    public abstract void onSync(DatabaseHelper dbHelper, JSONObject object) throws JSONException, SQLException;

    public Integer getSyncId() {
        return syncId;
    }

    public void setSyncId(Integer syncId) {
        this.syncId = syncId;
    }

    public Integer getSyncVersion() {
        return syncVersion;
    }

    public void setSyncVersion(Integer syncVersion) {
        this.syncVersion = syncVersion;
    }

    public SyncState getSyncState() {
        return syncState;
    }

    public void setSyncState(SyncState syncState) {
        this.syncState = syncState;
    }

    public void markAsUpdated() {
        if (getSyncState() != SyncState.SYNCED)
            return; // only mark synced objects that haven't been marked as updated already

        setSyncVersion(getSyncVersion()+1);
        setSyncState(SyncState.UPDATED);
    }

    public abstract void fillSyncParams(RequestParams params);

    /**
     * Creates an instance of a syncable using its entityName.
     * Used when the server requests creation of a syncable.
     * All syncables must be creatable here.
     * @param entityName
     * @return
     */
    public static SyncableItem newInstance(Context context, String entityName) {
        if (entityName == "word") {
            Word word = new Word();
            word.setUserAccount(KiokuServerClient.getCurrentUserId(context));
            return word;
        }

        if (entityName == "word_information") {
            return new WordInformation();
        }

        if (entityName == "word_information_test_performance") {
            return new WordInformationTestPerformance();
        }

        if (entityName == "word_information_test_answer") {
            return new WordInformationTestAnswer();
        }

        throw new InvalidParameterException("entity not supported");
    }

    /**
     * Creates a syncable entity to mark the current one as deleted for the next sync.
     * @return
     */
    protected abstract SyncableItem createDeletedMarker();

    public SyncableItem getDeletedMarker() {
        if (getSyncState() == SyncState.NEW)
            return null; // unsynced items should not use a deleted marker

        SyncableItem deletedMarker = createDeletedMarker();
        deletedMarker.setSyncState(SyncState.DELETED);
        deletedMarker.setSyncId(getSyncId());
        deletedMarker.setSyncVersion(getSyncVersion());
        return deletedMarker;
    }

}
