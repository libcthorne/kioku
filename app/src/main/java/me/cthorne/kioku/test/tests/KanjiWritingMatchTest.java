package me.cthorne.kioku.test.tests;

import android.content.Context;
import android.view.View;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.KiokuServerClient;
import me.cthorne.kioku.MainActivity;
import me.cthorne.kioku.test.WordInformationTestType;
import me.cthorne.kioku.test.helpers.KanjiWriting;
import me.cthorne.kioku.test.stacks.matchstack.MatchStackHandler;
import me.cthorne.kioku.test.stacks.matchstack.MatchStackTest;
import me.cthorne.kioku.words.Word;
import me.cthorne.kioku.words.WordInformation;
import me.cthorne.kioku.words.WordInformationType;

/**
 * Created by chris on 18/02/16.
 */
public class KanjiWritingMatchTest extends MatchStackTest {

    @Override
    public MatchStackHandler createStackHandler() {
        return new MatchStackHandler() {
            @Override
            public View getTop(Context context, WordInformation wordInformation) {
                return KanjiWriting.getKanjiPromptTextView(context, getHelper(), wordInformation);
            }

            @Override
            public View getBottom(Context context, WordInformation wordInformation) {
                return KanjiWriting.getKanjiTextView(context, wordInformation);
            }

            @Override
            public QueryBuilder qbBottomOptions(QueryBuilder qb, WordInformation wordInformation) throws SQLException {
                Where where = qb.where();
                where.and(where.eq("informationType", wordInformation.getInformationType()),
                        where.not().eq("word_id", wordInformation.getWord().id),
                        where.isNotNull("informationBytes"), // bottom word forms must have kanji
                        where.not().like("informationBytes", new String(wordInformation.getInformationBytes()))); // no kanji the same as the answer
                return qb.groupBy("informationBytes"); // no dupes
            }

            @Override
            public void onShow(WordInformation wordInformation) {
                MainActivity.ttsSpeak(wordInformation);
            }
        };
    }

    @Override
    public WordInformationTestType getTestType() {
        return WordInformationTestType.KANJI_WRITING;
    }

    @Override
    public WordInformationType getTestWordInformationType() {
        return WordInformationType.WORD_FORM;
    }

    @Override
    public long countTotalBottomWordInformation(DatabaseHelper dbHelper) throws SQLException {
        Dao<Word, Integer> wordDao = dbHelper.getWordDao();
        QueryBuilder wordQb = wordDao.queryBuilder();
        wordQb.where().eq("userAccount_id", KiokuServerClient.getCurrentUserId(dbHelper.getContext()));

        Dao<WordInformation, Integer> wordInformationDao = dbHelper.getWordInformationDao();
        QueryBuilder informationQb = wordInformationDao.queryBuilder();

        Where where = informationQb.where();
        where.and(where.eq("informationType", WordInformationType.WORD_FORM),
                where.isNotNull("informationBytes")); // word forms must have kanji

        // http://stackoverflow.com/a/29929816/5402565
        // Can't use countOf here for some reason
        return informationQb.groupBy("informationBytes").join(wordQb).query().size();
    }

    @Override
    public String getWhereConditionsForTesting() {
        // Only test forms with kanji and kana
        return "(" + super.getWhereConditionsForTesting() + ") AND " +
                "`word_informations`.`informationBytes` IS NOT NULL AND " +
                "`word_informations`.`metaInformationBytes` IS NOT NULL";
    }

    @Override
    public void processCorrectAnswer(WordInformation wordInformation, float secondsTaken) {
        super.processCorrectAnswer(wordInformation, secondsTaken);
        MainActivity.ttsSpeak(wordInformation);
    }

    @Override
    public void processIncorrectAnswer(WordInformation wordInformation, float secondsTaken) {
        super.processIncorrectAnswer(wordInformation, secondsTaken);
        MainActivity.ttsSpeak(wordInformation);
    }
}

