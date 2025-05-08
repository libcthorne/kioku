package me.cthorne.kioku;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.io.File;

import me.cthorne.kioku.auth.UserAccount;
import me.cthorne.kioku.infosources.SelectedWordInformationSource;
import me.cthorne.kioku.infosources.WordInformationSource;
import me.cthorne.kioku.languages.SelectedWordLanguage;
import me.cthorne.kioku.sync.SyncableItem.SyncState;
import me.cthorne.kioku.test.WordInformationTest;
import me.cthorne.kioku.test.WordInformationTestAnswer;
import me.cthorne.kioku.test.WordInformationTestPerformance;
import me.cthorne.kioku.test.WordInformationTestType;
import me.cthorne.kioku.words.Word;
import me.cthorne.kioku.words.WordInformation;
import me.cthorne.kioku.words.WordInformationType;
import me.cthorne.kioku.words.WordLanguage;

/**
 * Created by chris on 07/11/15.
 * Initally adapted from:
 * https://github.com/j256/ormlite-examples/blob/master/android/HelloAndroid/src/com/example/helloandroid/DatabaseHelper.java
 * http://www.horaceheaven.com/android-ormlite-tutorial/
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    public static final String DATABASE_NAME = "kioku";
    private static final int DATABASE_VERSION = 67;

    private Context context;

    /**
     * The data access objects used to interact with the Sqlite database to do C.R.U.D operations.
     */
    private Dao<UserAccount, Integer> userAccountDao;
    private Dao<Word, Integer> wordDao;
    private Dao<WordInformation, Integer> wordInformationDao;
    private Dao<WordInformationTestPerformance, Integer> wordInformationTestPerformanceDao;
    private Dao<WordInformationTestAnswer, Integer> wordInformationTestAnswerDao;
    private Dao<WordInformationSource, Integer> wordInformationSourceDao;
    private Dao<SelectedWordInformationSource, Integer> selectedWordInformationSourceDao;
    private Dao<SelectedWordLanguage, Integer> selectedWordLanguageDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION,
                /**
                 * R.raw.ormlite_config is a reference to the ormlite_config.txt file in the
                 * /res/raw/ directory of this project
                 * */
                R.raw.ormlite_config);

        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        Log.d("kioku-db", "onCreate");

        try {
            /**
             * creates the database tables
             */
            TableUtils.createTableIfNotExists(connectionSource, UserAccount.class);
            TableUtils.createTableIfNotExists(connectionSource, Word.class);
            TableUtils.createTableIfNotExists(connectionSource, WordInformation.class);
            TableUtils.createTableIfNotExists(connectionSource, WordInformationTestPerformance.class);
            TableUtils.createTableIfNotExists(connectionSource, WordInformationTestAnswer.class);
            TableUtils.createTableIfNotExists(connectionSource, WordInformationSource.class);
            TableUtils.createTableIfNotExists(connectionSource, SelectedWordInformationSource.class);
            TableUtils.createTableIfNotExists(connectionSource, SelectedWordLanguage.class);

            /**
             * create anonymous UserAccount
             */
            UserAccount userAccount = new UserAccount(UserAccount.ANONYMOUS_ID);
            getUserAccountDao().create(userAccount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            /**
             * Recreates the database when onUpgrade is called by the framework
             */
            TableUtils.dropTable(connectionSource, SelectedWordLanguage.class, true);
            TableUtils.dropTable(connectionSource, SelectedWordInformationSource.class, true);
            TableUtils.dropTable(connectionSource, WordInformationSource.class, true);
            TableUtils.dropTable(connectionSource, WordInformationTestAnswer.class, true);
            TableUtils.dropTable(connectionSource, WordInformationTestPerformance.class, true);
            TableUtils.dropTable(connectionSource, WordInformation.class, true);
            TableUtils.dropTable(connectionSource, Word.class, true);
            TableUtils.dropTable(connectionSource, UserAccount.class, true);

            onCreate(database, connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns an instance of the UserAccount DAO
     *
     * @return
     * @throws SQLException
     */
    public Dao<UserAccount, Integer> getUserAccountDao() throws SQLException {
        if (userAccountDao == null)
            userAccountDao = getDao(UserAccount.class);

        return userAccountDao;
    }

    /**
     * Returns an instance of the Word DAO
     *
     * @return
     * @throws SQLException
     */
    public Dao<Word, Integer> getWordDao() throws SQLException {
        if (wordDao == null)
            wordDao = getDao(Word.class);

        return wordDao;
    }

    /**
     * Returns an instance of the WordInformation DAO
     *
     * @return
     * @throws SQLException
     */
    public Dao<WordInformation, Integer> getWordInformationDao() throws SQLException {
        if (wordInformationDao == null)
            wordInformationDao = getDao(WordInformation.class);

        return wordInformationDao;
    }

    /**
     * Returns an instance of the WordInformationTestPerformance DAO
     *
     * @return
     * @throws SQLException
     */
    public Dao<WordInformationTestPerformance, Integer> getWordInformationTestPerformanceDao() throws SQLException {
        if (wordInformationTestPerformanceDao == null)
            wordInformationTestPerformanceDao = getDao(WordInformationTestPerformance.class);

        return wordInformationTestPerformanceDao;
    }

    /**
     * Returns an instance of the WordInformationTestAnswer DAO
     *
     * @return
     * @throws SQLException
     */
    public Dao<WordInformationTestAnswer, Integer> getWordInformationTestAnswerDao() throws SQLException {
        if (wordInformationTestAnswerDao == null)
            wordInformationTestAnswerDao = getDao(WordInformationTestAnswer.class);

        return wordInformationTestAnswerDao;
    }

    /**
     * Returns an instance of the WordInformationSource DAO
     *
     * @return
     * @throws SQLException
     */
    public Dao<WordInformationSource, Integer> getWordInformationSourceDao() throws SQLException {
        if (wordInformationSourceDao == null)
            wordInformationSourceDao = getDao(WordInformationSource.class);

        return wordInformationSourceDao;
    }

    /**
     * Returns an instance of the SelectedWordInformationSource DAO
     *
     * @return
     * @throws SQLException
     */
    public Dao<SelectedWordInformationSource, Integer> getSelectedWordInformationSourceDao() throws SQLException {
        if (selectedWordInformationSourceDao == null)
            selectedWordInformationSourceDao = getDao(SelectedWordInformationSource.class);

        return selectedWordInformationSourceDao;
    }

    /**
     * Returns an instance of the SelectedWordLanguage DAO
     *
     * @return
     * @throws SQLException
     */
    public Dao<SelectedWordLanguage, Integer> getSelectedWordLanguageDao() throws SQLException {
        if (selectedWordLanguageDao == null)
            selectedWordLanguageDao = getDao(SelectedWordLanguage.class);

        return selectedWordLanguageDao;
    }



    // Unfortunately ORMLite doesn't support multiple JOIN ON conditions (needed for joining with the 'performances' table) so a raw query needs to be used for this
    private static final String wordInformationsQueryConditions =
            " LEFT JOIN `words` ON" +
                    " `words`.`id` = `word_informations`.`word_id`" +
            " LEFT JOIN `word_information_test_performances` ON" +
                    " (`word_informations`.`id` = `word_information_test_performances`.`wordInformation_id`)" +
                    " AND (`word_information_test_performances`.`testType` = ?)" +
            " WHERE `words`.`userAccount_id` = ?" +
                    " AND `words`.`language` = ?" +
                    " AND `words`.`syncState` <> " + SyncState.DELETED.ordinal() +
                    " AND `word_informations`.`informationType` = ?" +
                    " AND ((`word_information_test_performances`.`id` IS NULL" +
                    "    OR `word_information_test_performances`.`nextDue` <= ?" +
                    "    OR `word_information_test_performances`.`nextDue` IS NULL ) )";


    private String[] getWordInformationForTestQueryParams(WordInformationTest test, WordInformationType informationType) {
        return new String[]{test.getTestType().toString(),
                KiokuServerClient.getCurrentUserIdStr(context),
                String.valueOf(MainActivity.currentLanguage.getValue()),
                informationType.toString(),
                String.valueOf(Utils.getLocalMidnightUTC())};
    }

    /**
     * Get word information IDs for a specific test that is due to be seen.
     *
     * @param test
     * @param informationType
     * @return
     * @throws SQLException
     */
    public List<Integer> getWordInformationIdsForTest(WordInformationTest test, WordInformationType informationType) throws SQLException {
        ArrayList<Integer> wordInformationIds = new ArrayList<>();

        GenericRawResults<Object[]> wordInformationIdsRaw =
                getWordInformationDao().queryRaw("SELECT `word_informations`.`id` FROM `word_informations`" + wordInformationsQueryConditions + " AND (" + test.getWhereConditionsForTesting() + ") ORDER BY RANDOM()",
                        new DataType[]{DataType.INTEGER},
                        getWordInformationForTestQueryParams(test, informationType));

        for (Object[] row : wordInformationIdsRaw.getResults())
            wordInformationIds.add((Integer)row[0]);

        wordInformationIdsRaw.close();

        return wordInformationIds;
    }

    /**
     * Get word information for a specific test that is due to be seen.
     *
     * @param test
     * @param informationType
     * @return
     * @throws SQLException
     */
    public List<WordInformation> getWordInformationForTest(WordInformationTest test, WordInformationType informationType) throws SQLException {
        ArrayList<WordInformation> wordInformations = new ArrayList<>();

        GenericRawResults<WordInformation> wordInformationsRaw =
                getWordInformationDao().queryRaw("SELECT * FROM `word_informations`" + wordInformationsQueryConditions + " AND (" + test.getWhereConditionsForTesting() + ") ORDER BY RANDOM()",
                        WordInformation.getDataTypes(),
                        WordInformation.getObjectMapper(getWordDao()),
                        getWordInformationForTestQueryParams(test, informationType));

        for (WordInformation wordInformation : wordInformationsRaw.getResults())
            wordInformations.add(wordInformation);

        wordInformationsRaw.close();

        return wordInformations;
    }

    /**
     * Count word information for a specific test that is due to be seen.
     *
     * @param test
     * @param informationType
     * @return
     * @throws SQLException
     */
    public long countWordInformationForTest(WordInformationTest test, WordInformationType informationType) throws SQLException {
        Log.d("kioku-db", "counting for test (user: " + KiokuServerClient.getCurrentUserIdStr(context) + "): " + test.getTestType().toString() + "," + informationType.toString());

        //return qbWordInformationForTest(test, informationType).countOf();
        long count = getWordInformationDao().queryRawValue("SELECT COUNT(*) FROM `word_informations`" + wordInformationsQueryConditions + " AND (" + test.getWhereConditionsForTesting() + ")",
                getWordInformationForTestQueryParams(test, informationType));

        Log.d("kioku-db", "count: " + count + " (language: " + MainActivity.currentLanguage.getValue() + ")");

        return count;
    }

    /**
     * Count total word information of a given type.
     *
     * @param informationType
     * @return
     * @throws SQLException
     */
    public long countWordInformation(WordInformationType informationType) throws SQLException {
        Dao<WordInformation, Integer> wordInformationDao = getWordInformationDao();

        QueryBuilder wordQb = qbUserWords();
        QueryBuilder informationQb = wordInformationDao.queryBuilder();
        informationQb.where().eq("informationType", informationType);

        informationQb.join(wordQb);

        return informationQb.countOf();
    }

    /**
     * Checks to see if a UserAccount exists with given email; otherwise creates it.
     * @param email
     * @return the user account
     * @throws SQLException
     */
    public UserAccount createOrFindUserAccountByEmail(String email) throws SQLException {
        Dao<UserAccount, Integer> userAccountDao = getUserAccountDao();
        QueryBuilder qb = userAccountDao.queryBuilder();

        UserAccount userAccount = (UserAccount) qb.where().eq("email", email).queryForFirst();

        if (userAccount == null) {
            userAccount = new UserAccount(email);
            userAccountDao.create(userAccount);
        }

        return userAccount;
    }

    /**
     * Counts the number of anonymous words, i.e. words with no user, in the database.
     * @return the number of words with no user
     * @throws SQLException
     */
    public long countWordsWithNoUser() throws SQLException {
        Dao<Word, Integer> wordDao = getWordDao();
        QueryBuilder qb = wordDao.queryBuilder();
        return qb.where().eq("userAccount_id", UserAccount.ANONYMOUS_ID).countOf();
    }

    /**
     * Converts all words in the database with no user to be owned by specified user account.
     * @param userAccountId the new owner of the words
     * @throws SQLException
     */
    public void convertAnonymousWordsToUserWords(int userAccountId) throws SQLException {
        Log.d("kioku-db", "converting anon words to words for UserAccount:" + userAccountId);

        // Daos
        final Dao<Word, Integer> wordDao = getWordDao();
        Dao<UserAccount, Integer> userAccountDao = getUserAccountDao();

        // Get current user account
        final UserAccount userAccount = userAccountDao.queryForId(userAccountId);

        // Query anonymous account words
        QueryBuilder qb = wordDao.queryBuilder();
        qb.where().eq("userAccount_id", UserAccount.ANONYMOUS_ID);

        final List<Word> words = qb.query();

        // Update words' UserAccount field in transaction
        TransactionManager.callInTransaction(getConnectionSource(),
                new Callable<Void>() {
                    public Void call() throws Exception {

                        for (Word word : words) {
                            word.setUserAccount(userAccount);
                            wordDao.update(word);
                        }

                        return null;
                    }
                });
    }

    /**
     * Builds a where statement for words that belong to the current user, with the option of including deleted markers, and using only current language.
     * @return
     * @throws SQLException
     */
    public Where<Word, Integer> whereUserWords(QueryBuilder wordQb, boolean includeDeleted, boolean currentLanguageOnly) throws SQLException {
        Where where = wordQb.where();
        where.eq("userAccount_id", KiokuServerClient.getCurrentUserId(context));

        if (!includeDeleted)
            where.and().ne("syncState", SyncState.DELETED);

        if (currentLanguageOnly)
            where.and().eq("language", MainActivity.currentLanguage.getValue());

        return where;
    }

    /**
     * Builds a where statement for words that belong to the current user, not including deleted words and only those of the current language.
     * @return
     * @throws SQLException
     */
    public Where<Word, Integer> whereUserWords(QueryBuilder wordQb) throws SQLException {
        return whereUserWords(wordQb, false, true);
    }

    /**
     * Builds a query for words that belong to the current user, with the option of including deleted markers.
     * @return
     * @throws SQLException
     */
    public QueryBuilder<Word, Integer> qbUserWords(boolean includeDeleted, boolean currentLanguageOnly) throws SQLException {
        Dao<Word, Integer> wordDao = getWordDao();

        QueryBuilder wordQb = wordDao.queryBuilder();
        whereUserWords(wordQb, includeDeleted, currentLanguageOnly);

        return wordQb;
    }

    /**
     * Builds a query for words that belong to the current user for the current language.
     * @return
     * @throws SQLException
     */
    public QueryBuilder<Word, Integer> qbUserWords() throws SQLException {
        return qbUserWords(false, true);
    }

    /**
     * Builds a query for word informations that belong to the current user, with the option of including deleted markers.
     * @return
     * @throws SQLException
     */
    public QueryBuilder<WordInformation, Integer> qbUserWordInformations(boolean includeDeleted, boolean currentLanguageOnly) throws SQLException {
        QueryBuilder wordQb = qbUserWords(includeDeleted, currentLanguageOnly);
        QueryBuilder infoQb = getWordInformationDao().queryBuilder();
        return infoQb.join(wordQb);
    }

    /**
     * Builds a query for word informations that belong to the current user for the current language.
     * @return
     * @throws SQLException
     */
    public QueryBuilder<WordInformation, Integer> qbUserWordInformations() throws SQLException {
        return qbUserWordInformations(false, true);
    }


    /**
     * Builds a query for word information test performances that belong to the current user, with the option of including deleted markers.
     * @param includeDeleted
     * @param currentLanguageOnly
     * @return
     * @throws SQLException
     */
    public QueryBuilder<WordInformationTestPerformance, Integer> qbUserWordInformationTestPerformances(boolean includeDeleted, boolean currentLanguageOnly) throws SQLException {
        QueryBuilder infoQb = qbUserWordInformations(includeDeleted, currentLanguageOnly);
        QueryBuilder performanceQb = getWordInformationTestPerformanceDao().queryBuilder();

        return performanceQb.join(infoQb);
    }

    /**
     * Builds a query for word information test answers that belong to the current user, with the option of including deleted markers.
     * @param includeDeleted
     * @param currentLanguageOnly
     * @return
     * @throws SQLException
     */
    public QueryBuilder qbUserWordInformationTestAnswers(boolean includeDeleted, boolean currentLanguageOnly) throws SQLException {
        QueryBuilder infoQb = qbUserWordInformations(includeDeleted, currentLanguageOnly);
        QueryBuilder answerQb = getWordInformationTestAnswerDao().queryBuilder();

        return answerQb.join(infoQb);
    }

    /**
     * Builds a query that selects all the SelectedWordInformationSources for a given user.
     * @return
     * @throws SQLException
     */
    public QueryBuilder<SelectedWordInformationSource, Integer> qbSelectedUserSources(boolean enabledOnly, WordLanguage language) throws SQLException {
        Dao<SelectedWordInformationSource, Integer> selectedSourceDao = getSelectedWordInformationSourceDao();

        QueryBuilder selectedSourceQb = selectedSourceDao.queryBuilder();

        Where where = selectedSourceQb.where();
        where.eq("userAccount_id", KiokuServerClient.getCurrentUserId(context));
        if (enabledOnly)
            where.and().eq("enabled", true);
        where.and().eq("language", language.getValue());

        // Sort by ascending position
        return selectedSourceQb.orderBy("position", true);
    }

    /**
     * Builds a query that selects all the WordInformationSources for a given user.
     * @return
     * @throws SQLException
     */
    public QueryBuilder<WordInformationSource, Integer> qbUserSources(boolean enabledOnly, WordLanguage language) throws SQLException {
        Dao<WordInformationSource, Integer> sourceDao = getWordInformationSourceDao();
        QueryBuilder sourceQb = sourceDao.queryBuilder();

        QueryBuilder selectedSourceQb = qbSelectedUserSources(enabledOnly, language);

        return sourceQb.join(selectedSourceQb);
    }

    /**
     * Builds a query that selects all the WordInformationTestAnswers with given testType and responseQuality for current user.
     * @return
     * @throws SQLException
     */
    public QueryBuilder<WordInformationTestAnswer, Integer> qbUserTestAnswers(WordInformationTestType testType, int responseQuality) throws SQLException {
        Dao<WordInformationTestAnswer, Integer> testAnswerDao = getWordInformationTestAnswerDao();

        QueryBuilder testAnswerQb = testAnswerDao.queryBuilder();
        testAnswerQb.where().eq("testType", testType)
                .and().eq("responseQuality", responseQuality);

        QueryBuilder qbUserWordInformations = qbUserWordInformations();

        return testAnswerQb.join(qbUserWordInformations);
    }

    /**
     * Builds a query that selects all the WordInformationTestAnswers with given testType and responseQuality for current user for given date range.
     * @return
     * @throws SQLException
     */
    public QueryBuilder qbUserTestAnswersRange(WordInformationTestType testType, int responseQuality, Date start, Date end) throws SQLException {
        Dao<WordInformationTestAnswer, Integer> testAnswerDao = getWordInformationTestAnswerDao();

        QueryBuilder testAnswerQb = testAnswerDao.queryBuilder();
        testAnswerQb.where().eq("testType", testType)
                .and().eq("responseQuality", responseQuality)
                .and().ge("createdAt", start)
                .and().le("createdAt", end);

        QueryBuilder qbUserWordInformations = qbUserWordInformations();

        return testAnswerQb.join(qbUserWordInformations);
    }

    /**
     * Builds a query that selects all the WordInformationTestAnswers with given testType and responseQuality for current user for current week.
     * @return
     * @throws SQLException
     */
    public QueryBuilder qbUserTestAnswersThisWeek(WordInformationTestType testType, int responseQuality) throws SQLException {
        DateTime today = new DateTime();
        Date weekStart = today.withDayOfWeek(DateTimeConstants.MONDAY).withTimeAtStartOfDay().toDate();
        Date weekEnd = today.plusWeeks(1).withDayOfWeek(DateTimeConstants.MONDAY).withTimeAtStartOfDay().toDate();

        return qbUserTestAnswersRange(testType, responseQuality, weekStart, weekEnd);
    }

    /**
     * Builds a query that selects all the WordInformationTestAnswers with given testType and responseQuality for current user for current month.
     * @return
     * @throws SQLException
     */
    public QueryBuilder qbUserTestAnswersThisMonth(WordInformationTestType testType, int responseQuality) throws SQLException {
        DateTime today = new DateTime();

        Date monthStart = today.withDayOfMonth(1).withTimeAtStartOfDay().toDate();
        Date monthEnd = today.plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay().toDate();

        return qbUserTestAnswersRange(testType, responseQuality, monthStart, monthEnd);
    }

    /**
     * Builds a query that selects all the WordInformationTestAnswers with given testType and responseQuality for current user for current year.
     * @return
     * @throws SQLException
     */
    public QueryBuilder qbUserTestAnswersThisYear(WordInformationTestType testType, int responseQuality) throws SQLException {
        DateTime today = new DateTime();

        Date yearStart = today.withDayOfYear(1).withTimeAtStartOfDay().toDate();
        Date yearEnd = today.plusYears(1).withDayOfYear(1).withTimeAtStartOfDay().toDate();

        return qbUserTestAnswersRange(testType, responseQuality, yearStart, yearEnd);
    }

    /**
     * Sums the answer times (in seconds) for a given day range.
     * @return
     * @throws SQLException
     */
    public float sumAnswerSecondsForDateRange(DateTime start, DateTime end) throws SQLException {
        Dao<WordInformationTestAnswer, Integer> testAnswerDao = getWordInformationTestAnswerDao();

        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

        // Couldn't get this working without using a raw query
        QueryBuilder testAnswerQb = testAnswerDao.queryBuilder();
        testAnswerQb.where()
                .raw("`word_information_test_answers`.`createdAt` >= datetime('" + f.print(start) + "')")
                .and().raw("`word_information_test_answers`.`createdAt` < datetime('" + f.print(end) + "')");

        QueryBuilder qbUserWordInformations = qbUserWordInformations();

        // Select using a raw query to get a float result
        testAnswerQb = testAnswerQb.join(qbUserWordInformations).selectRaw("SUM(secondsTaken)");

        //Log.d("kioku-db", "sum: " + testAnswerQb.prepareStatementString());

        float r = (float)testAnswerDao.queryRaw(testAnswerQb.prepareStatementString(),
                                                new DataType[]{DataType.FLOAT})
                                      .getFirstResult()[0];

        //Log.d("kioku-db", "r: " + r);

        return r;
    }

    /**
     * Sums the answer times (in seconds) for a given day.
     * @return
     * @throws SQLException
     */
    public float sumAnswerSecondsForDay(DateTime date) throws SQLException {
        DateTime todayStart = date.withTimeAtStartOfDay();
        DateTime tomorrowStart = date.plusDays(1).withTimeAtStartOfDay();

        return sumAnswerSecondsForDateRange(todayStart, tomorrowStart);
    }

    /**
     * Builds a query to select the current user's selected languages of study.
     * @return
     * @throws SQLException
     */
    public QueryBuilder<SelectedWordLanguage, Integer> qbUserLanguages() throws SQLException {
        Dao<SelectedWordLanguage, Integer> selectedWordLanguageQb = getSelectedWordLanguageDao();

        QueryBuilder<SelectedWordLanguage, Integer> qb = selectedWordLanguageQb.queryBuilder();

        Where where = qb.where();
        where.eq("userAccount_id", KiokuServerClient.getCurrentUserId(context));

        return qb;
    }

    /**
     * Gets a list of the current user's selected languages of study.
     * @return
     * @throws SQLException
     */
    public List<SelectedWordLanguage> getUserLanguages() throws SQLException {
        return qbUserLanguages().orderBy("id", true).query();
    }

    /**
     * Counts how many languages the current user has selected for study.
     * @return
     * @throws SQLException
     */
    public long countUserLanguages() throws SQLException {
        return qbUserLanguages().countOf();
    }

    public List<WordInformation> getWordInformationsForWord(Word word, WordInformationType informationType) throws SQLException {
        QueryBuilder<WordInformation, Integer> qb = getWordInformationDao().queryBuilder();
        Where where = qb.where();
        where.and(where.eq("word_id", word.id),
                where.eq("informationType", informationType));

        return qb.query();
    }

    /**
     * Clears all tables in the database
     */
    public void clearAllTables() throws SQLException {
        TableUtils.clearTable(getConnectionSource(), SelectedWordLanguage.class);
        TableUtils.clearTable(getConnectionSource(), SelectedWordInformationSource.class);
        TableUtils.clearTable(getConnectionSource(), WordInformationSource.class);
        TableUtils.clearTable(getConnectionSource(), WordInformationTestAnswer.class);
        TableUtils.clearTable(getConnectionSource(), WordInformationTestPerformance.class);
        TableUtils.clearTable(getConnectionSource(), WordInformation.class);
        TableUtils.clearTable(getConnectionSource(), Word.class);
        TableUtils.clearTable(getConnectionSource(), UserAccount.class);
    }

    /**
     * Clears the cache directory
     */
    public void clearCache() {
        File cacheDir = context.getCacheDir();
        if (cacheDir != null && cacheDir.exists()) {
            deleteDir(cacheDir);
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    /**
     * Exports data to a file
     */
    public void exportData() throws SQLException {
        // TODO: Implement data export functionality
        throw new SQLException("Data export not implemented yet");
    }

    /**
     * Imports data from a file
     */
    public void importData() throws SQLException {
        // TODO: Implement data import functionality
        throw new SQLException("Data import not implemented yet");
    }
}
