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
import me.cthorne.kioku.test.helpers.Sentence;
import me.cthorne.kioku.test.stacks.matchstack.MatchStackHandler;
import me.cthorne.kioku.test.stacks.matchstack.MatchStackTest;
import me.cthorne.kioku.words.Word;
import me.cthorne.kioku.words.WordInformation;
import me.cthorne.kioku.words.WordInformationType;

/**
 * Created by chris on 18/02/16.
 */
public class SentenceMatchTest extends MatchStackTest {

    @Override
    public MatchStackHandler createStackHandler() {
        return new MatchStackHandler() {
            @Override
            public View getTop(Context context, WordInformation wordInformation) {
                return Sentence.getSentenceTextView(context, wordInformation);
            }

            @Override
            public View getBottom(Context context, WordInformation wordInformation) {
                return Sentence.getSentenceTranslationTextView(context, getHelper(), wordInformation);
            }

            @Override
            public QueryBuilder qbBottomOptions(QueryBuilder qb, WordInformation wordInformation) throws SQLException {
                Where where = qb.where();
                where.and(
                        where.eq("informationType", wordInformation.getInformationType()),
                        where.isNotNull("metaInformationBytes"), // bottom options must have translations
                        where.not().eq("id", wordInformation.id)
                );

                return qb;
            }

            @Override
            public void onShow(WordInformation wordInformation) {
                MainActivity.ttsSpeak(wordInformation);
            }
        };
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
                where.eq("informationType", WordInformationType.SENTENCE),
                where.isNotNull("metaInformationBytes")
        ); // bottom sentences must have a translation

        // http://stackoverflow.com/a/29929816/5402565
        // Can't use countOf here for some reason
        return informationQb.join(wordQb).query().size();
    }

    @Override
    public String getWhereConditionsForTesting() {
        // Only test sentences that have a translation
        return "(" + super.getWhereConditionsForTesting() + ") AND " +
                "`word_informations`.`informationBytes` IS NOT NULL AND " +
                "`word_informations`.`metaInformationBytes` IS NOT NULL";
    }

    @Override
    public WordInformationTestType getTestType() {
        return WordInformationTestType.SENTENCE_COMPREHENSION;
    }

    @Override
    public WordInformationType getTestWordInformationType() {
        return WordInformationType.SENTENCE;
    }

}
