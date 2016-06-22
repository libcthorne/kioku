package me.cthorne.kioku.words;

import android.util.Base64;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RawRowObjectMapper;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Arrays;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.sync.SyncableItem;

/**
 * Created by chris on 05/01/16.
 */
@DatabaseTable(tableName = "word_informations")
public class WordInformation extends SyncableItem {

    // The word the information belongs to
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnDefinition = "integer references words(id) on delete cascade")
    private Word word;

    // The type of the information (image, definition, etc.)
    @DatabaseField(unknownEnumName = "UNKNOWN")
    private WordInformationType informationType;

    // Byte array containing the information about the word (image, definition string, etc.)
    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    private byte[] informationBytes;

    // Byte array containing information about informationBytes (e.g. translation of a Japanese sentence, reading of kanji)
    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    private byte[] metaInformationBytes;

    /**
     * Used by queryRaw in DatabaseHelper.getWordInformationForTest.
     * @return
     */
    public static DataType[] getDataTypes() {
        return new DataType[]{DataType.INTEGER, DataType.ENUM_STRING, DataType.BYTE_ARRAY, DataType.BYTE_ARRAY, DataType.INTEGER, DataType.INTEGER_OBJ, DataType.INTEGER_OBJ, DataType.ENUM_INTEGER};
    }

    /**
     * Used by queryRaw in DatabaseHelper.getWordInformationForTest.
     * @return
     */
    public static RawRowObjectMapper<WordInformation> getObjectMapper(final Dao<Word, Integer> wordDao) {
        return new RawRowObjectMapper<WordInformation>() {

            @Override
            public WordInformation mapRow(String[] columnNames, DataType[] dataTypes, Object[] resultColumns) throws SQLException {
                Log.d("kioku-db", "mapping WordInformation");

                Word word = wordDao.queryForId((Integer)resultColumns[0]);
                WordInformationType wordInformationType = WordInformationType.valueOf((String) resultColumns[1]);
                byte[] b1 = (byte[])resultColumns[2];
                byte[] b2 = (byte[])resultColumns[3];

                WordInformation wordInformation = new WordInformation(wordInformationType, word, b1, b2);
                wordInformation.id = (int)resultColumns[4];
                wordInformation.setSyncId((Integer)resultColumns[5]);
                wordInformation.setSyncVersion((Integer)resultColumns[6]);
                wordInformation.setSyncState(SyncState.values()[(int)resultColumns[7]]);

                return wordInformation;
            }
        };
    }

    public WordInformation() {
        // ORMLite constructor
    }

    public WordInformation(WordInformationType informationType, byte[] informationBytes) {
        this.informationType = informationType;
        this.informationBytes = informationBytes;
    }

    public WordInformation(WordInformationType informationType, byte[] informationBytes, byte[] metaInformationBytes) {
        this(informationType, informationBytes);
        this.metaInformationBytes = metaInformationBytes;
    }

    public WordInformation(String informationType, byte[] informationBytes) {
        this(WordInformationType.valueOf(informationType), informationBytes);
    }

    public WordInformation(String informationType, byte[] informationBytes, byte[] metaInformationBytes) {
        this(WordInformationType.valueOf(informationType), informationBytes, metaInformationBytes);
    }

    public WordInformation(WordInformationType informationType, Word word, byte[] informationBytes) {
        this.informationType = informationType;
        this.word = word;
        this.informationBytes = informationBytes;
    }

    public WordInformation(WordInformationType informationType, Word word, byte[] informationBytes, byte[] metaInformationBytes) {
        this(informationType, word, informationBytes);
        this.metaInformationBytes = metaInformationBytes;
    }

    public WordInformation(WordInformationType informationType, Word word) {
        this.informationType = informationType;
        this.word = word;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public byte[] getInformationBytes() {
        return informationBytes;
    }

    public void setInformationBytes(byte[] informationBytes) {
        if (!Arrays.equals(informationBytes, this.metaInformationBytes))
            markAsUpdated();

        this.informationBytes = informationBytes;
    }

    public byte[] getMetaInformationBytes() {
        return metaInformationBytes;
    }

    public void setMetaInformationBytes(byte[] metaInformationBytes) {
        if (!Arrays.equals(metaInformationBytes, this.metaInformationBytes))
            markAsUpdated();

        this.metaInformationBytes = metaInformationBytes;
    }

    public WordInformationType getInformationType() { return informationType; }

    public void setInformationType(WordInformationType informationType) {
        this.informationType = informationType;
    }

    @Override
    public void onSync(DatabaseHelper dbHelper, JSONObject object) throws JSONException, SQLException {
        Dao<Word, Integer> wordDao = dbHelper.getWordDao();

        this.word = wordDao.queryBuilder().where().eq("syncId", object.getInt("word_sync_id")).queryForFirst();
        this.informationType = WordInformationType.valueOf(object.getString("information_type"));

        if (object.has("information_bytes_base64"))
            this.informationBytes = Base64.decode(object.getString("information_bytes_base64"), Base64.DEFAULT);
        if (object.has("meta_information_bytes_base64"))
            this.metaInformationBytes = Base64.decode(object.getString("meta_information_bytes_base64"), Base64.DEFAULT);
    }

    @Override
    public void fillSyncParams(RequestParams params) {
        params.put("word_sync_id", word.getSyncId());
        params.put("information_type", informationType.toString());
        if (informationBytes != null)
            params.put("information_bytes_base64", new String(Base64.encode(informationBytes, Base64.DEFAULT)));
        if (metaInformationBytes != null)
            params.put("meta_information_bytes_base64", new String(Base64.encode(metaInformationBytes, Base64.DEFAULT)));
    }

    @Override
    protected SyncableItem createDeletedMarker() {
        WordInformation wordInformation = new WordInformation();
        wordInformation.setWord(getWord());
        return wordInformation;
    }
}
