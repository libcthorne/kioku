package me.cthorne.kioku.words;

import android.util.Log;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.KiokuServerClient;
import me.cthorne.kioku.Utils;
import me.cthorne.kioku.WordForm;
import me.cthorne.kioku.auth.UserAccount;
import me.cthorne.kioku.sync.SyncableItem;

/**
 * Created by chris on 07/11/15.
 */
@DatabaseTable(tableName = "words")
public class Word extends SyncableItem {
    // The user account the word belongs to
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = false, columnDefinition = "integer references user_accounts(id) on delete cascade")
    private UserAccount userAccount;

    @DatabaseField(canBeNull = false)
    public Integer language;

    @DatabaseField(canBeNull = false, dataType = DataType.DATE_LONG)
    public Date createdAt;

    // A collection of information associated with this word
    // eager is set to false so this is not loaded unless used
    @ForeignCollectionField(eager = false)
    private ForeignCollection<WordInformation> informations;

    public Word() {
        // ORMLite constructor
    }

    public List<WordTranslation> getTranslations(DatabaseHelper dbHelper) {
        try {
            List<WordInformation> relevantInformations = dbHelper.getWordInformationsForWord(this, WordInformationType.TRANSLATION);

            ArrayList<WordTranslation> translations = new ArrayList<>();
            for (WordInformation information : relevantInformations)
                translations.add(new WordTranslation(information));

            return translations;
        } catch (SQLException e) {
            return null;
        }
    }

    public String getTranslationString(DatabaseHelper dbHelper) {
        String translationString = "";
        for (WordTranslation translation : getTranslations(dbHelper)) {
            if (!translationString.isEmpty())
                translationString += "\n"; // Line break for multiple translations

            translationString += translation.getText();
        }

        return translationString;
    }

    public List<WordDefinition> getDefinitions(DatabaseHelper dbHelper) {
        try {
            List<WordInformation> relevantInformations = dbHelper.getWordInformationsForWord(this, WordInformationType.DEFINITION);

            ArrayList<WordDefinition> definitions = new ArrayList<>();
            for (WordInformation information : relevantInformations)
                definitions.add(new WordDefinition(information));

            return definitions;
        } catch (SQLException e) {
            return null;
        }
    }

    public String getDefinitionString(DatabaseHelper dbHelper) {
        String definitionString = "";
        for (WordDefinition definition : getDefinitions(dbHelper)) {
            if (definitionString.length() > 0)
                definitionString += "\n"; // Line break for multiple definitions

            definitionString += definition.getText();
        }

        return definitionString;
    }

    public List<WordForm> getForms(DatabaseHelper dbHelper) {
        try {
            List<WordInformation> relevantInformations = dbHelper.getWordInformationsForWord(this, WordInformationType.WORD_FORM);
            ArrayList<WordForm> forms = new ArrayList<>();
            for (WordInformation information : relevantInformations) {
                forms.add(new WordForm(information));
            }

            return forms;
        } catch (SQLException e) {
            return null;
        }
    }

    public String getWordString(DatabaseHelper dbHelper) {
        return getWordString(dbHelper, "\n", "?");
    }

    public String getWordString(DatabaseHelper dbHelper, String formSeparator, String unknownString) {
        try {
            List<WordInformation> relevantInformations = dbHelper.getWordInformationsForWord(this, WordInformationType.WORD_FORM);

            if (relevantInformations.size() == 0)
                return unknownString;

            String formsString = "";

            for (WordInformation information : relevantInformations) {
                byte[] informationBytes = information.getInformationBytes();
                byte[] metaInformationBytes = information.getMetaInformationBytes();

                if (!formsString.isEmpty())
                    formsString += formSeparator;

                if (informationBytes != null) {
                    String kanjiString = new String(informationBytes);

                    if (metaInformationBytes != null) {
                        kanjiString += " (";
                        kanjiString += new String(metaInformationBytes);
                        kanjiString += ")";
                    }

                    formsString += kanjiString;
                } else if (metaInformationBytes != null) {
                    String kanaString = new String(metaInformationBytes);
                    formsString += kanaString;
                }
            }

            return formsString;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return unknownString;
    }

    public String getWordStringKanaPreferred(DatabaseHelper dbHelper) {
        try {
            List<WordInformation> relevantInformations = dbHelper.getWordInformationsForWord(this, WordInformationType.WORD_FORM);

            String formsString = "";

            for (WordInformation information : relevantInformations) {
                byte[] informationBytes = information.getInformationBytes();
                byte[] metaInformationBytes = information.getMetaInformationBytes();

                if (!formsString.isEmpty())
                    formsString += "\n";

                if (metaInformationBytes != null)
                    formsString += new String(metaInformationBytes); // kana
                else if (informationBytes != null)
                    formsString += new String(informationBytes); // kanji
            }

            return formsString;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "unknown";
    }

    public List<WordSentence> getSentences(DatabaseHelper dbHelper) {
        try {
            List<WordInformation> relevantInformations = dbHelper.getWordInformationsForWord(this, WordInformationType.SENTENCE);
            ArrayList<WordSentence> sentences = new ArrayList<>();
            for (WordInformation information : relevantInformations) {
                sentences.add(new WordSentence(information));
            }

            return sentences;
        } catch (SQLException e) {
            return null;
        }
    }

    public List<WordImage> getImages(DatabaseHelper dbHelper) {
        try {
            List<WordInformation> relevantInformations = dbHelper.getWordInformationsForWord(this, WordInformationType.IMAGE);
            ArrayList<WordImage> images = new ArrayList<>();
            for (WordInformation information : relevantInformations) {
                images.add(new WordImage(information));
            }

            return images;
        } catch (SQLException e) {
            return null;
        }
    }

    public UserAccount getUserAccount() {
        return this.userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public void setUserAccount(int userAccountId) {
        this.userAccount = new UserAccount(userAccountId);
    }

    public Integer getLanguage() {
        return this.language;
    }

    public void setLanguage(int language) {
        this.language = language;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public void onSync(DatabaseHelper dbHelper, JSONObject object) throws JSONException, SQLException {
        this.language = object.getInt("language");

        try {
            this.createdAt = Utils.parseRailsDateTime(object.getString("created_at"));

        } catch (IllegalArgumentException e) {
            throw new JSONException("Invalid DateTime format.");
        }

        setUserAccount(KiokuServerClient.getCurrentUserId(dbHelper.getContext()));

        Log.d("kioku-sync", "word createdAt: " + this.createdAt.toString());
    }

    @Override
    public void fillSyncParams(RequestParams params) {
        params.add("language", String.valueOf(language));
        params.add("created_at", createdAt.toString());
    }

    @Override
    protected SyncableItem createDeletedMarker() {
        Word word = new Word();
        word.setUserAccount(getUserAccount());
        word.setLanguage(getLanguage());
        word.setCreatedAt(getCreatedAt());
        return word;
    }
}