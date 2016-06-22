package me.cthorne.kioku.auth;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by chris on 17/01/16.
 * Stores local accounts the user has signed into on this device.
 */
@DatabaseTable(tableName = "user_accounts")
public class UserAccount {

    // ID of anonymous user account
    public final static int ANONYMOUS_ID = 1;

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    public int id;

    @DatabaseField
    private String email;

    public UserAccount() {
        // ORMLite constructor
    }

    public UserAccount(int id) {
        this.id = id;
    }

    public UserAccount(String email) {
        this.email = email;
    }

}
