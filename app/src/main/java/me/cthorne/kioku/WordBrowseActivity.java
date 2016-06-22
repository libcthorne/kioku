package me.cthorne.kioku;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivityCompat;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import me.cthorne.kioku.words.Word;

public class WordBrowseActivity extends OrmLiteBaseActivityCompat<DatabaseHelper> implements AdapterView.OnItemClickListener {

    private ArrayList<Word> userWords;
    private ListView mainListView;
    private WordBrowseListViewAdapter arrayAdapter;

    private Word newWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_browse);

        userWords = new ArrayList<>();

        // Adapter for user words list view
        arrayAdapter = new WordBrowseListViewAdapter(this, getHelper(), userWords);

        // List view
        mainListView = (ListView) findViewById(R.id.main_listview);
        mainListView.setAdapter(arrayAdapter);
        mainListView.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            userWords.clear();
            userWords.addAll(Arrays.asList(getUserWords()));

            arrayAdapter.notifyDataSetChanged();
        } catch (SQLException e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "Error retrieving words", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_word_browse, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_word_button:
                createEmptyWord();
                break;
        }

        return(super.onOptionsItemSelected(item));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Word word = (Word) arrayAdapter.getItem(position);

        Intent viewIntent = new Intent(this, WordViewActivity.class);
        viewIntent.putExtra("wordId", word.id);
        startActivity(viewIntent);
    }

    private Word[] getUserWords() throws SQLException {
        QueryBuilder<Word, Integer> qb = getHelper().qbUserWords().orderByRaw("syncId IS NULL ASC, syncId ASC"); // nulls last
        //QueryBuilder<Word, Integer> qb = dbHelper.qbUserWords();
        //qb.where().isNull("syncId");
        Log.d("kioku-db", "getUserWords: " + qb.prepareStatementString());
        List<Word> words = qb.query(); // nulls last

        // Only show non-empty words
        // Empty words will be deleted automatically and should not be shown
        // Slightly hacky
        List<Word> nonEmptyWords = new ArrayList<>();
        for (Word word : words) {
            if (getHelper().getWordInformationDao().queryBuilder().where().eq("word_id", word.id).countOf() > 0)
                nonEmptyWords.add(word);
        }

        return nonEmptyWords.toArray(new Word[nonEmptyWords.size()]);
    }

    private void createEmptyWord() {
        newWord = new Word();
        newWord.setUserAccount(KiokuServerClient.getCurrentUserId(this));
        newWord.setLanguage(MainActivity.currentLanguage.getValue());
        newWord.createdAt = new Date();

        try {
            getHelper().getWordDao().create(newWord);

            // Show word edit
            Intent wordEditIntent = new Intent(this, WordViewActivity.class);
            wordEditIntent.putExtra("wordId", newWord.id);
            startActivity(wordEditIntent);
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating word", Toast.LENGTH_SHORT).show();
        }
    }
}
