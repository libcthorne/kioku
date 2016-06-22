package me.cthorne.kioku.infosources;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.KiokuServerClient;
import me.cthorne.kioku.auth.UserAccount;
import me.cthorne.kioku.sync.SyncableItem;
import me.cthorne.kioku.words.WordLanguage;

/**
 * Created by chris on 23/01/16.
 */
@DatabaseTable(tableName = "selected_word_information_sources")
public class SelectedWordInformationSource extends SyncableItem {

    // The user account the word information source is for
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = false, columnDefinition = "integer references user_accounts(id) on delete cascade", uniqueCombo = true)
    private UserAccount userAccount;

    // The selected word source
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = false, uniqueCombo = true)
    private WordInformationSource source;

    // The language the word source is selected for
    @DatabaseField(canBeNull = false, uniqueCombo = true)
    private Integer language;

    // Whether or not the source should be used in word searches
    @DatabaseField(canBeNull = false)
    private Boolean enabled;

    // Position in list of all selected word sources (first=1, second=2, ...)
    @DatabaseField(canBeNull = false)
    private Integer position;

    public SelectedWordInformationSource() {
        // ORMLite constructor
    }

    public SelectedWordInformationSource(Context context, WordInformationSource source, WordLanguage language, int position) {
        this.userAccount = new UserAccount(KiokuServerClient.getCurrentUserId(context));
        this.source = source;
        this.language = language.getValue();
        this.position = position;
        this.enabled = true;
    }

    @Override
    public void onSync(DatabaseHelper dbHelper, JSONObject object) throws JSONException, SQLException {
        // TODO
    }

    @Override
    public void fillSyncParams(RequestParams params) {
        // TODO
    }

    @Override
    protected SyncableItem createDeletedMarker() {
        return new SelectedWordInformationSource();
    }

    public void loadSource(DatabaseHelper dbHelper) throws SQLException {
        if (source.getVersion() == 0) {
            // Try and load
            WordInformationSource thisSource = dbHelper.getWordInformationSourceDao().queryBuilder().where().eq("name", source.getName()).queryForFirst();

            if (thisSource != null)
                source = thisSource;
        }

        if (source.getVersion() > 0) {
            // Loaded
            Log.d("kioku-sources", "has source: " + source.getName() + "," + source.getVersion());
        } else {
            // Source missing
            Log.d("kioku-sources", "empty source: " + source.getName());
        }
    }

    public WordInformationSource getSource() {
        return source;
    }

    public String getSourceName() {
        return source.getName();
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}