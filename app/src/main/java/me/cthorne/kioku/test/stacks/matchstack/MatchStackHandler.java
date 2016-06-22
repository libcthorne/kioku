package me.cthorne.kioku.test.stacks.matchstack;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 12/01/16.
 */
public abstract class MatchStackHandler implements Serializable {
    public abstract View getTop(Context context, WordInformation wordInformation);
    public abstract View getBottom(Context context, WordInformation wordInformation);

    public void onShow(WordInformation wordInformation) {
    }

    public QueryBuilder qbBottomOptions(QueryBuilder qb, WordInformation wordInformation) throws SQLException {
        Log.d("kioku-db", "wordID: " + wordInformation.getWord().id);
        Where where = qb.where();
        where.and(where.eq("informationType", wordInformation.getInformationType()),
                where.not().eq("word_id", wordInformation.getWord().id));

        // select only one piece of information for each word
        // useful in ImageToWordMatchTest, for example
        return qb.groupBy("word_id");
    }

    public ArrayList<WordInformation> getBottomOptions(DatabaseHelper dbHelper, WordInformation wordInformation) {
        try {
            Dao<WordInformation, Integer> wordInformationDao = dbHelper.getWordInformationDao();

            QueryBuilder wordQb = dbHelper.qbUserWords();
            QueryBuilder informationQb = qbBottomOptions(wordInformationDao.queryBuilder(), wordInformation);
            informationQb = informationQb.orderByRaw("RANDOM()").limit(3L);

            Log.d("kioku-db", informationQb.prepareStatementString());
            List<WordInformation> otherWordInformations = informationQb.leftJoin(wordQb).query();

            ArrayList<WordInformation> list = new ArrayList<>();
            // Correct answer
            list.add(wordInformation);
            // Incorrect answers
            list.addAll(otherWordInformations);
            // Shuffle so correct isn't always first
            Collections.shuffle(list, new Random());

            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
