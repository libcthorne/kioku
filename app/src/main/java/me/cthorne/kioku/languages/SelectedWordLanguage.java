package me.cthorne.kioku.languages;

import android.content.Context;

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
 * Created by chris on 08/02/16.
 * Holds languages the user has selected for study.
 */
@DatabaseTable(tableName = "selected_word_languages")
public class SelectedWordLanguage extends SyncableItem {

    // The user account studying the language
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = false, columnDefinition = "integer references user_accounts(id) on delete cascade", uniqueCombo = true)
    private UserAccount userAccount;

    // The selected language
    @DatabaseField(canBeNull = false, uniqueCombo = true)
    private Integer language;

    public SelectedWordLanguage() {
        // ORMLite constructor
    }

    public SelectedWordLanguage(Context context, WordLanguage language) {
        this.userAccount = new UserAccount(KiokuServerClient.getCurrentUserId(context));
        this.language = language.getValue();
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
        return new SelectedWordLanguage();
    }

    public WordLanguage getLanguage() {
        return WordLanguage.fromInt(language);
    }

    @Override
    public boolean equals(Object object) {
        if (object != null && object instanceof SelectedWordLanguage) {
            return (this.userAccount.id == ((SelectedWordLanguage)object).userAccount.id) &&
                    (this.getLanguage().getValue() == ((SelectedWordLanguage)object).getLanguage().getValue());
        }

        return false;
    }
}