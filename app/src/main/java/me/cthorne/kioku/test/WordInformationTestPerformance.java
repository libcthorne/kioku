package me.cthorne.kioku.test;

import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.SM2;
import me.cthorne.kioku.Utils;
import me.cthorne.kioku.sync.SyncableItem;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 07/01/16.
 */
@DatabaseTable(tableName = "word_information_test_performances")
public class WordInformationTestPerformance extends SyncableItem {

    // The type of test
    @DatabaseField(unknownEnumName = "UNKNOWN", index = true, uniqueCombo = true)
    private WordInformationTestType testType;

    // The information tested
    @DatabaseField(canBeNull = false, index = true, foreign = true, foreignAutoRefresh = true, columnDefinition = "integer references word_informations(id) on delete cascade", uniqueCombo = true)
    private WordInformation wordInformation;

    // Spaced repetition values
    @DatabaseField(dataType = DataType.DATE_LONG)
    private Date lastSeen; // When the information was last seen
    @DatabaseField(dataType = DataType.DATE_LONG)
    private Date nextDue; // When the information should next be seen
    @DatabaseField
    private int interval;
    @DatabaseField
    public int easinessFactor;

    public WordInformationTestPerformance() {
        // ORMLite constructor
    }

    public WordInformationTestPerformance(WordInformation wordInformation, WordInformationTestType testType) {
        this.wordInformation = wordInformation;
        this.testType = testType;
        this.easinessFactor = SM2.DEFAULT_EASINESS_FACTOR;
    }

    public WordInformationTestType getTestType() { return testType; }

    public WordInformation getWordInformation() {
        return wordInformation;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public Date getNextDue() {
        return nextDue;
    }

    public void setNextDue(Date date) {
        this.nextDue = date;

        markAsUpdated();
    }

    public void resetRepetitionValues() {
        // Get current date's 00:00
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        easinessFactor = SM2.DEFAULT_EASINESS_FACTOR;
        interval = 0;
        lastSeen = cal.getTime();
        nextDue = cal.getTime();

        markAsUpdated();
    }

    public void updateRepetitionValues(int responseQuality) {
        // Set new easiness factor
        easinessFactor = SM2.calculateNewEasinessFactor(easinessFactor, responseQuality);

        // Set last seen to now
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        lastSeen = cal.getTime();

        // Set to 00:00 for nextDue
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // Set new repetition interval
        if (responseQuality == 1) {
            Log.d("kioku-sm2", "interval: " + interval + " -> " + SM2.calculateNextInterval(interval, easinessFactor));
            interval = SM2.calculateNextInterval(interval, easinessFactor);
            cal.add(Calendar.DATE, interval); // Add new interval to current day to get nextDue
        } else {
            Log.d("kioku-sm2", "interval: " + interval);
            interval = 0; // Reset interval on incorrect answer
        }

        // Set when to next show the card
        Log.d("kioku-sm2", "nextDue: " + nextDue + " -> " + cal.getTime());
        nextDue = cal.getTime();

        markAsUpdated();
    }

    @Override
    public void onSync(DatabaseHelper dbHelper, JSONObject object) throws JSONException, SQLException {
        Dao<WordInformation, Integer> wordInformationDao = dbHelper.getWordInformationDao();

        this.testType = WordInformationTestType.valueOf(object.getString("test_type"));
        this.wordInformation = wordInformationDao.queryBuilder().where().eq("syncId", object.getInt("word_information_sync_id")).queryForFirst();
        this.lastSeen = Utils.parseRailsDateTime(object.getString("last_seen"));
        this.nextDue = Utils.parseRailsDateTime(object.getString("next_due"));
        this.interval = object.getInt("interval");
        this.easinessFactor = object.getInt("easiness_factor");
    }

    @Override
    public void fillSyncParams(RequestParams params) {
        params.put("test_type", testType.toString());
        params.put("word_information_sync_id", wordInformation.getSyncId());
        params.put("last_seen", lastSeen.toString());
        params.put("next_due", nextDue.toString());
        params.put("interval", interval);
        params.put("easiness_factor", easinessFactor);
    }

    @Override
    protected SyncableItem createDeletedMarker() {
        return new WordInformationTestPerformance(getWordInformation(), getTestType());
    }
}