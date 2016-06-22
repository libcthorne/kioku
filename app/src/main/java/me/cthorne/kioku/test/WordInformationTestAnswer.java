package me.cthorne.kioku.test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Date;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.Utils;
import me.cthorne.kioku.sync.SyncableItem;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 31/01/16.
 */
@DatabaseTable(tableName = "word_information_test_answers")
public class WordInformationTestAnswer extends SyncableItem {

    // The type of test
    @DatabaseField(unknownEnumName = "UNKNOWN", index = true)
    private WordInformationTestType testType;

    // The information tested
    @DatabaseField(canBeNull = false, index = true, foreign = true, foreignAutoRefresh = true, columnDefinition = "integer references word_informations(id) on delete cascade")
    private WordInformation wordInformation;

    // Quality of response (0 = forgot, 1 = remembered)
    @DatabaseField(canBeNull = false, index = true)
    private Integer responseQuality;

    // DateTime of answer
    @DatabaseField(canBeNull = false, dataType = DataType.DATE_LONG)
    private Date createdAt;

    // Time taken to answer in seconds
    @DatabaseField(canBeNull = false)
    public Float secondsTaken;

    public WordInformationTestAnswer() {
        // ORMLite constructor
    }

    public WordInformationTestAnswer(WordInformation wordInformation) {
        this.wordInformation = wordInformation;
    }

    public WordInformationTestAnswer(WordInformation wordInformation, WordInformationTestType testType, int responseQuality, float secondsTaken) {
        this.wordInformation = wordInformation;
        this.testType = testType;
        this.responseQuality = responseQuality;
        this.createdAt = new Date();
        this.secondsTaken = secondsTaken;
    }

    public WordInformation getWordInformation() {
        return wordInformation;
    }

    @Override
    public void onSync(DatabaseHelper dbHelper, JSONObject object) throws JSONException, SQLException {
        Dao<WordInformation, Integer> wordInformationDao = dbHelper.getWordInformationDao();

        this.testType = WordInformationTestType.valueOf(object.getString("t"));
        this.wordInformation = wordInformationDao.queryBuilder().where().eq("syncId", object.getInt("w")).queryForFirst();
        this.responseQuality = object.getInt("r");
        this.createdAt = Utils.parseRailsDateTime(object.getString("c"));
        this.secondsTaken = Float.parseFloat(object.getString("s"));
    }

    @Override
    public void fillSyncParams(RequestParams params) {
        params.put("t", testType.toString());
        params.put("w", wordInformation.getSyncId());
        params.put("r", responseQuality);
        params.put("c", createdAt.toString());
        params.put("s", secondsTaken.toString());
    }

    @Override
    protected SyncableItem createDeletedMarker() {
        return new WordInformationTestAnswer(getWordInformation());
    }

}