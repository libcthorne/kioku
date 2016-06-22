package me.cthorne.kioku.test;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.MainActivity;
import me.cthorne.kioku.words.WordInformation;
import me.cthorne.kioku.words.WordInformationType;

/**
 * Created by chris on 07/01/16.
 */
public abstract class WordInformationTest {

    public interface OnTestFinishedHandler {
        void onFinish();
    }

    public static List<Integer> wordInformationIdsList;

    public static WordInformationTestActivity activity;

    private OnTestFinishedHandler finishedHandler;

    public abstract WordInformationTestType getTestType();

    /**
     * Gets the type of information the test is testing.
     * @return information type
     */
    public abstract WordInformationType getTestWordInformationType();

    public static DatabaseHelper getHelper() {
        return activity.getHelper();
    }

    public abstract void startTest();

    public void startTest(List<Integer> wordInformationIdsList, OnTestFinishedHandler finishedHandler) {
        this.wordInformationIdsList = wordInformationIdsList;
        this.finishedHandler = finishedHandler;

        startTest();
    }

    public void startTest(DatabaseHelper dbHelper, OnTestFinishedHandler finishedHandler) {
        try {
            if (wordInformationIdsList == null || wordInformationIdsList.isEmpty())
                wordInformationIdsList = dbHelper.getWordInformationIdsForTest(this, getTestWordInformationType());

            // For resuming activities, move the previous word information ID to the start of the list
            // by removing all previous entries
            if (WordInformationTestActivity.currentWordInformation != null) {
                int idx = wordInformationIdsList.lastIndexOf(WordInformationTestActivity.currentWordInformation.id);

                wordInformationIdsList = wordInformationIdsList.subList(idx, wordInformationIdsList.size());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            getTestActivity().finish();
            Toast.makeText(getTestActivity(), "Error getting word information list", Toast.LENGTH_SHORT).show();
            return;
        }

        startTest(wordInformationIdsList, finishedHandler);
    }

    public void finishTest() {
        // Clear any word lists from previous active tests
        WordInformationTest.wordInformationIdsList = null;
        WordInformationTestActivity.currentWordInformation = null;

        // Stop TTS
        MainActivity.ttsStop();

        finishedHandler.onFinish();
    }

    public abstract void resetTest();

    public abstract long countNumberOfTests(DatabaseHelper dbHelper);
    public abstract int getContentView();

    public boolean canStartTest(DatabaseHelper dbHelper) {
        return countNumberOfTests(dbHelper) > 0;
    }

    public Activity getTestActivity() {
        return activity;
    }

    public void setTestActivity(WordInformationTestActivity activity) {
        this.activity = activity;
    }

    public void dbSaveAnswer(final WordInformation wordInformation, final int responseQuality, final float secondsTaken) {
        final DatabaseHelper dbHelper = getHelper();

        try {
            TransactionManager.callInTransaction(dbHelper.getConnectionSource(), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    // Get DAOs
                    Dao<WordInformationTestPerformance, Integer> wordInformationTestPerformanceDao = dbHelper.getWordInformationTestPerformanceDao();
                    Dao<WordInformationTestAnswer, Integer> wordInformationTestAnswerDao = dbHelper.getWordInformationTestAnswerDao();

                    // First check if test performance exists
                    /*QueryBuilder<WordInformationTestPerformance, Integer> qb = wordInformationTestPerformanceDao.queryBuilder();
                    Where where = qb.where();
                    where.and(where.eq("testType", getTestType()),
                            where.eq("wordInformation_id", wordInformation.id));
                    Log.d("kioku-test", "cnt: " + qb.query().size());*/

                    Where<WordInformationTestPerformance, Integer> where = wordInformationTestPerformanceDao.queryBuilder().where();
                    where.and(where.eq("testType", getTestType()),
                            where.eq("wordInformation_id", wordInformation.id));
                    WordInformationTestPerformance testPerformance = where.queryForFirst();

                    if (testPerformance == null) {
                        Log.d("kioku-db", "[" + secondsTaken + "s] new test performance for word information[" + wordInformation.id + "] (" + getTestType().toString() + ")");

                        // Create new
                        testPerformance = new WordInformationTestPerformance(wordInformation, getTestType());
                        wordInformationTestPerformanceDao.create(testPerformance);
                    } else {
                        Log.d("kioku-db", "[" + secondsTaken + "s] updating existing test performance for word information[" + wordInformation.id + "] (" + getTestType().toString() + ")");
                    }

                    // Update repetition values for correct answer
                    testPerformance.updateRepetitionValues(responseQuality);

                    // Update performance
                    wordInformationTestPerformanceDao.update(testPerformance);

                    // Store record of answer
                    WordInformationTestAnswer answer = new WordInformationTestAnswer(wordInformation, getTestType(), responseQuality, secondsTaken);
                    wordInformationTestAnswerDao.create(answer);

                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(getTestActivity(), "Error saving test answer", Toast.LENGTH_SHORT).show();
            getTestActivity().finish();
        }
    }

    public void processCorrectAnswer(WordInformation wordInformation, float secondsTaken) {
        activity.playCorrectSound();
    }

    public void processIncorrectAnswer(WordInformation wordInformation, float secondsTaken) {
        activity.playIncorrectSound();
    }

    public String getWhereConditionsForTesting() {
        return "1"; // Empty condition
    }

}