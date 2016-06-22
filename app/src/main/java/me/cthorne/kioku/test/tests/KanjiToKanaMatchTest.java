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
import me.cthorne.kioku.test.helpers.KanjiToKana;
import me.cthorne.kioku.test.stacks.matchstack.MatchStackHandler;
import me.cthorne.kioku.test.stacks.matchstack.MatchStackTest;
import me.cthorne.kioku.words.Word;
import me.cthorne.kioku.words.WordInformation;
import me.cthorne.kioku.words.WordInformationType;

/**
 * Created by chris on 13/01/16.
 */
public class KanjiToKanaMatchTest extends MatchStackTest {

    @Override
    public MatchStackHandler createStackHandler() {
        return new MatchStackHandler() {
            @Override
            public View getTop(Context context, WordInformation wordInformation) {
                return KanjiToKana.getKanjiTextView(context, wordInformation);
            }

            @Override
            public View getBottom(Context context, WordInformation wordInformation) {
                return KanjiToKana.getKanaTextView(context, wordInformation);
            }

            @Override
            public QueryBuilder qbBottomOptions(QueryBuilder qb, WordInformation wordInformation) throws SQLException {
                String metaString = wordInformation.getMetaInformationBytes() != null ?
                                    new String(wordInformation.getMetaInformationBytes()) : "";

                Where where = qb.where();
                where.and(where.eq("informationType", wordInformation.getInformationType()),
                        where.not().eq("word_id", wordInformation.getWord().id),
                        where.isNotNull("metaInformationBytes"), // bottom word forms must have kana
                        where.not().like("metaInformationBytes", metaString)); // no kana the same as the answer
                return qb.groupBy("metaInformationBytes"); // no dupes
            }
        };
    }

    @Override
    public WordInformationTestType getTestType() {
        return WordInformationTestType.KANJI_READING;
    }

    @Override
    public WordInformationType getTestWordInformationType() {
        return WordInformationType.WORD_FORM;
    }

    @Override
    public long countTotalBottomWordInformation(DatabaseHelper dbHelper) throws SQLException {
        Dao<Word, Integer> wordDao = dbHelper.getWordDao();
        QueryBuilder wordQb = wordDao.queryBuilder();
        Where where = wordQb.where();
        where.and(
                where.eq("userAccount_id", KiokuServerClient.getCurrentUserId(dbHelper.getContext())),
                where.eq("language", MainActivity.currentLanguage.getValue())
        );

        Dao<WordInformation, Integer> wordInformationDao = dbHelper.getWordInformationDao();
        QueryBuilder informationQb = wordInformationDao.queryBuilder();

        where = informationQb.where();
        where.and(
                where.eq("informationType", WordInformationType.WORD_FORM),
                where.isNotNull("metaInformationBytes")
        ); // bottom word forms must have kana

        // http://stackoverflow.com/a/29929816/5402565
        // Can't use countOf here for some reason
        return informationQb.groupBy("metaInformationBytes").join(wordQb).query().size();
    }

    @Override
    public String getWhereConditionsForTesting() {
        // Only test forms with kanji and kana
        return "(" + super.getWhereConditionsForTesting() + ") AND " +
                "`word_informations`.`informationBytes` IS NOT NULL";
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
