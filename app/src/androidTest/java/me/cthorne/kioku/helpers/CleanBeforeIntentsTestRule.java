package me.cthorne.kioku.helpers;

import android.app.Activity;
import android.content.Context;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.KiokuServerClient;
import me.cthorne.kioku.auth.UserAccount;
import me.cthorne.kioku.infosources.SelectedWordInformationSource;
import me.cthorne.kioku.infosources.WordInformationSource;
import me.cthorne.kioku.languages.SelectedWordLanguage;
import me.cthorne.kioku.test.WordInformationTestAnswer;
import me.cthorne.kioku.test.WordInformationTestPerformance;
import me.cthorne.kioku.words.Word;
import me.cthorne.kioku.words.WordInformation;

import static android.support.test.InstrumentationRegistry.getTargetContext;

/**
 * Created by chris on 12/03/16.
 */
public class CleanBeforeIntentsTestRule<T extends Activity> extends IntentsTestRule {
    private PostCleanCallback postCleanCallback;

    public interface PostCleanCallback {
        void run(Context context);
    }

    public CleanBeforeIntentsTestRule(Class activityClass) {
        super(activityClass);
    }

    public CleanBeforeIntentsTestRule(Class activityClass, PostCleanCallback postCleanCallback) {
        this(activityClass);
        this.postCleanCallback = postCleanCallback;
    }

    @Override
    public void beforeActivityLaunched() {
        KiokuServerClient.getPreferences(getTargetContext()).edit().clear().commit();
        //getTargetContext().deleteDatabase(DatabaseHelper.DATABASE_NAME);

        Context context = getTargetContext();
        DatabaseHelper dbHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        ConnectionSource connectionSource = dbHelper.getConnectionSource();

        try {
            TableUtils.clearTable(connectionSource, SelectedWordLanguage.class);
            TableUtils.clearTable(connectionSource, SelectedWordInformationSource.class);
            TableUtils.clearTable(connectionSource, WordInformationSource.class);
            TableUtils.clearTable(connectionSource, WordInformationTestAnswer.class);
            TableUtils.clearTable(connectionSource, WordInformationTestPerformance.class);
            TableUtils.clearTable(connectionSource, WordInformation.class);
            TableUtils.clearTable(connectionSource, Word.class);
            TableUtils.clearTable(connectionSource, UserAccount.class);

            /**
             * create anonymous UserAccount
             */
            UserAccount userAccount = new UserAccount(UserAccount.ANONYMOUS_ID);
            dbHelper.getUserAccountDao().create(userAccount);


            for (UserAccount ua : dbHelper.getUserAccountDao().queryForAll()) {
                Log.d("kioku-test", "ua: " + Integer.toString(ua.id));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        OpenHelperManager.releaseHelper();

        if (postCleanCallback != null)
            postCleanCallback.run(getTargetContext());

        super.beforeActivityLaunched();
    }
}
