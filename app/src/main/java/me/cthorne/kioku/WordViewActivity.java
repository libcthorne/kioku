package me.cthorne.kioku;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivityCompat;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.SelectArg;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import me.cthorne.kioku.words.Word;
import me.cthorne.kioku.words.WordDefinition;
import me.cthorne.kioku.words.WordImage;
import me.cthorne.kioku.words.WordInformation;
import me.cthorne.kioku.words.WordInformationType;
import me.cthorne.kioku.words.WordLanguage;
import me.cthorne.kioku.words.WordSentence;
import me.cthorne.kioku.words.WordTranslation;

/**
 * Created by chris on 01/11/15.
 */
public class WordViewActivity extends OrmLiteBaseActivityCompat<DatabaseHelper> {
    private static final int SELECT_PHOTO = 100;
    private static final int WORD_SEARCH_MENU_GROUP = 1;
    private static final int WORD_SEARCH_CUSTOM = 0;
    private static final int WORD_SEARCH_ITEM = 1;

    private int wordId;
    private Word word;

    private LinearLayout wordViewContainer;

    private LinearLayout viewWordForms;
    private LinearLayout viewTranslations;
    private LinearLayout viewDefinitions;
    private LinearLayout viewSentences;
    private LinearLayout viewImages;

    private Button addWordFormButton;
    private Button addTranslationButton;
    private Button addDefinitionButton;
    private Button addSentenceButton;
    private Button addImageButton;

    private TextView firstHint;

    private boolean searchWord;
    private boolean newWord;

    private SubMenu searchSubMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Activity activity = this;
        
        wordId = getIntent().getExtras().getInt("wordId");

        try {
            word = getHelper().getWordDao().queryForId(wordId);
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error editing word", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        wordViewContainer = (LinearLayout)findViewById(R.id.word_view_container);
        wordViewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("kioku-edit", "container click");
                clearFocusAll();
            }
        });
        wordViewContainer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d("kioku-edit", "container focus");
                Utils.hideKeyboard(activity);
            }
        });

        viewWordForms = (LinearLayout)findViewById(R.id.view_word_forms);
        viewTranslations = (LinearLayout)findViewById(R.id.view_translations);
        viewDefinitions = (LinearLayout)findViewById(R.id.view_definitions);
        viewSentences = (LinearLayout)findViewById(R.id.view_sentences);
        viewImages = (LinearLayout)findViewById(R.id.view_images);

        addWordFormButton = (Button)findViewById(R.id.add_word_form_button);
        addWordFormButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add view for info and grab focus on first EditText (+ show keyboard)
                EditText editableForm = (EditText)addWordFormView(null).getChildAt(0);
                editableForm.requestFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editableForm, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        addTranslationButton = (Button)findViewById(R.id.add_translation_button);
        addTranslationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add view for info and grab focus on first EditText (+ show keyboard)
                EditText editableForm = (EditText)addTranslationView(null).getChildAt(0);
                editableForm.requestFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editableForm, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        addDefinitionButton = (Button)findViewById(R.id.add_definition_button);
        addDefinitionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add view for info and grab focus on first EditText (+ show keyboard)
                EditText editableForm = (EditText)addDefinitionView(null).getChildAt(0);
                editableForm.requestFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editableForm, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        addSentenceButton = (Button)findViewById(R.id.add_sentence_button);
        addSentenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add view for info and grab focus on first EditText (+ show keyboard)
                EditText editableSentence = (EditText)addSentenceView(null).getChildAt(0);
                editableSentence.requestFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editableSentence, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        addImageButton = (Button)findViewById(R.id.add_image_button);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });

        firstHint = (TextView)findViewById(R.id.first_hint);

        if (MainActivity.isInTutorial())
            firstHint.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SearchActivity.WORD_SEARCH_RESULT_ID:
                if (resultCode == Activity.RESULT_OK)
                    finish();
                break;
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri selectedImageUri = imageReturnedIntent.getData();

                    Log.d("kioku-edit", "got selected image: " + selectedImageUri);

                    try {
                        Bitmap selectedImage = Utils.decodeUri(this, selectedImageUri);

                        try {
                            // Save image
                            String fileName = Utils.saveBitmapToFile(this, selectedImage);

                            // Create word information object for image
                            WordInformation wordInformation = new WordInformation(WordInformationType.IMAGE, fileName.getBytes());
                            wordInformation.setWord(word);

                            // Save to DB
                            getHelper().getWordInformationDao().create(wordInformation);

                            // Add image view
                            WordImage wordImage = new WordImage(wordInformation);
                            addImageView(wordImage);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("kioku-edit", "error saving selected image: " + e.getMessage());
                            Toast.makeText(this, "Error adding image", Toast.LENGTH_SHORT).show();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error adding image", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadWord();

        searchWord = getIntent().getExtras().getBoolean("searchWord", false);
        newWord = getIntent().getExtras().getBoolean("newWord", false);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d("kioku-edit", "paused WordViewActivity");

        clearFocusAll();
    }

    @Override
    protected void onDestroy() {
        try {
            boolean wordEmpty = getHelper().getWordInformationDao().queryBuilder().where().eq("word_id", word).countOf() == 0;

            if (wordEmpty) {
                Log.d("kioku-edit", "word empty; deleting");
                getHelper().getWordDao().delete(word);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        super.onDestroy();

        Log.d("kioku-edit", "destroy WordViewActivity");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("kioku-edit", "onOptionsItemSelected: " + item);

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            /*case R.id.edit_word_button:
                enterEditMode();
                return true;*/
            case R.id.save_word_button:
                saveWord();
                return true;
            case R.id.delete_word_button:
                requestDeleteWord();
                return true;
            case R.id.search_add_button:
                clearFocusAll();
                return true;
            default:
                if (item.getGroupId() == WORD_SEARCH_MENU_GROUP) {
                    if (item.getItemId() == WORD_SEARCH_CUSTOM) {
                        final Activity activity = this;

                        // Source: http://stackoverflow.com/a/10904665/5402565

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);


                        // Set up the input
                        final EditText input = new EditText(this);
                        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                        input.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
                        int customSearchPaddingPx = getResources().getDimensionPixelSize(R.dimen.custom_search_padding);
                        input.setPadding(customSearchPaddingPx, customSearchPaddingPx, customSearchPaddingPx, customSearchPaddingPx);
                        input.setHint("Enter search term");
                        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                            @Override
                            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                                    String searchTerm = input.getText().toString();
                                    if (searchTerm.length() == 0)
                                        return true;

                                    SearchActivity.searchWord(activity, getHelper(), searchTerm, word);
                                }

                                return false;
                            }
                        });
                        builder.setView(input);
                        AlertDialog dialog = builder.create();

                        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
                        wmlp.width = WindowManager.LayoutParams.MATCH_PARENT;

                        dialog.show();

                        Utils.showKeyboard(this);
                    } else {
                        String searchTerm = item.getTitle().toString();
                        SearchActivity.searchWord(this, getHelper(), searchTerm, word);
                    }

                    return true;
                }
        }

        return(super.onOptionsItemSelected(item));
    }

    private void clearFocusAll() {
        wordViewContainer.requestFocus();
        Utils.hideKeyboard(this);
    }

    private void loadWord() {
        DatabaseHelper dbHelper = getHelper();

        try {
            Dao<Word, Integer> wordDao = dbHelper.getWordDao();

            word = wordDao.queryForId(wordId);

            /*if (word == null) {
                word = new Word();
                word.setUserAccount(KiokuServerClient.getCurrentUserId());
                word.setCreatedAt(new Date());
                word.setLanguage(MainActivity.currentLanguage.getValue());
                wordDao.create(word);
            }*/

            // Add word information views
            addViews(word);

            // Update action bar title
            onInfoChange();
        } catch (SQLException e) {
            e.printStackTrace();
            finish();
        }
    }

    private void addViews(Word word) {
        viewWordForms.removeAllViews();
        for (WordForm form : word.getForms(getHelper()))
            addWordFormView(form);

        viewTranslations.removeAllViews();
        for (WordTranslation translation : word.getTranslations(getHelper()))
            addTranslationView(translation);

        viewDefinitions.removeAllViews();
        for (WordDefinition definition : word.getDefinitions(getHelper()))
            addDefinitionView(definition);

        viewSentences.removeAllViews();
        for (WordSentence sentence : word.getSentences(getHelper()))
            addSentenceView(sentence);

        viewImages.removeAllViews();
        for (WordImage image : word.getImages(getHelper()))
            addImageView(image);
    }

    private void loadTitle() {
        DatabaseHelper dbHelper = getHelper();

        try {
            Dao<Word, Integer> wordDao = dbHelper.getWordDao();

            Word word = wordDao.queryForId(wordId);

            String wordString = word.getWordString(dbHelper, ", ", "");
            // Set action bar title
            getSupportActionBar().setTitle(wordString);
        } catch (SQLException e) {
            e.printStackTrace();
            finish();
        }
    }

    /**
     * Gets all word forms and translations and adds them to search-add submenu for the user to search.
     */
    private void loadSearchTerms() {
        if (searchSubMenu == null)
            return;

        searchSubMenu.clear();

        DatabaseHelper dbHelper = getHelper();

        try {
            Dao<Word, Integer> wordDao = dbHelper.getWordDao();

            Word word = wordDao.queryForId(wordId);

            for (WordForm form : word.getForms(dbHelper)) {
                if (form.getForm() != null)
                    searchSubMenu.add(WORD_SEARCH_MENU_GROUP, WORD_SEARCH_ITEM, 0, form.getForm());

                if (form.getFormMeta() != null)
                    searchSubMenu.add(WORD_SEARCH_MENU_GROUP, WORD_SEARCH_ITEM, 0, form.getFormMeta());
            }

            for (WordTranslation translation : word.getTranslations(dbHelper)) {
                searchSubMenu.add(WORD_SEARCH_MENU_GROUP, WORD_SEARCH_ITEM, 0, translation.getText());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            finish();
        }

        searchSubMenu.add(WORD_SEARCH_MENU_GROUP, WORD_SEARCH_CUSTOM, 0, "+ Custom search");
    }


    private void onInfoChange() {
        loadTitle();
        loadSearchTerms();
    }

    private LinearLayout addWordFormView(WordForm form) {
        LinearLayout formLayout = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        formLayout.setLayoutParams(params);
        formLayout.setOrientation(LinearLayout.HORIZONTAL);
        formLayout.setTag(form == null ? 0 : form.getWordInformationId());
        formLayout.setTag(R.string.tag_information_type, WordInformationType.WORD_FORM);

        boolean showFormMeta = word.getLanguage() == WordLanguage.JP.getValue(); // Meta form only used in Japanese at the moment

        // Form information + meta information views

        EditText editableForm = new EditText(this);
        editableForm.setBackgroundResource(R.drawable.edittext_editable);
        if (form != null && form.getForm() != null)
            editableForm.setText(Html.fromHtml(form.getForm()));
        editableForm.setHint(WordLanguage.fromInt(word.getLanguage()).getWordFormInformationHint());
        editableForm.setSingleLine();
        editableForm.setOnFocusChangeListener(new WordInformationEditTextFocusListener(this));
        editableForm.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editableForm.setOnEditorActionListener(new WordInformationEditorActionListener());
        LinearLayout.LayoutParams formParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        formParams.weight = 4;
        formParams.topMargin = 5;
        formParams.leftMargin = 0;
        formParams.rightMargin = showFormMeta ? 10 : 0; // no margin if there is no meta form
        editableForm.setLayoutParams(formParams);

        formLayout.addView(editableForm);

        if (showFormMeta) {
            EditText editableFormMeta = new EditText(this);
            editableFormMeta.setBackgroundResource(R.drawable.edittext_editable);
            if (form != null && form.getFormMeta() != null)
                editableFormMeta.setText(Html.fromHtml(form.getFormMeta()));
            editableFormMeta.setHint(WordLanguage.fromInt(word.getLanguage()).getWordFormMetaInformationHint());
            editableFormMeta.setSingleLine();
            editableFormMeta.setOnFocusChangeListener(new WordInformationEditTextFocusListener(this));
            editableFormMeta.setImeOptions(EditorInfo.IME_ACTION_DONE);
            editableFormMeta.setOnEditorActionListener(new WordInformationEditorActionListener());
            LinearLayout.LayoutParams formMetaParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            formMetaParams.weight = 4;
            formMetaParams.topMargin = 5;
            formMetaParams.leftMargin = 10;
            formMetaParams.rightMargin = 0;
            editableFormMeta.setLayoutParams(formMetaParams);

            formLayout.addView(editableFormMeta);
        }

        addDeleteButton(formLayout);

        viewWordForms.addView(formLayout);

        return formLayout;
    }

    private LinearLayout addSingleLineView(int wordInformationId, String text, String hint, WordInformationType wordInformationType) {
        LinearLayout container = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(params);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setTag(wordInformationId);
        container.setTag(R.string.tag_information_type, wordInformationType);

        EditText editableText = new EditText(this);
        editableText.setBackgroundResource(R.drawable.edittext_editable);
        if (wordInformationId != 0)
            editableText.setText(Html.fromHtml(text));
        editableText.setHint(hint);
        editableText.setSingleLine();
        editableText.setOnFocusChangeListener(new WordInformationEditTextFocusListener(this));
        editableText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editableText.setOnEditorActionListener(new WordInformationEditorActionListener());
        // Enable text wrapping
        editableText.setHorizontallyScrolling(false);
        editableText.setMaxLines(Integer.MAX_VALUE);

        LinearLayout.LayoutParams editableTextParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        editableTextParams.topMargin = 5;
        editableTextParams.weight = 1;
        editableText.setLayoutParams(editableTextParams);

        container.addView(editableText);

        addDeleteButton(container);

        return container;
    }

    private LinearLayout addTranslationView(WordTranslation translation) {
        int wordInformationId = translation == null ? 0 : translation.getWordInformationId();
        String text = translation == null ? "" : translation.getText();

        LinearLayout container = addSingleLineView(wordInformationId, text, "Translation", WordInformationType.TRANSLATION);
        viewTranslations.addView(container);
        return container;
    }

    private LinearLayout addDefinitionView(WordDefinition definition) {
        int wordInformationId = definition == null ? 0 : definition.getWordInformationId();
        String text = definition == null ? "" : definition.getText();

        LinearLayout container = addSingleLineView(wordInformationId, text, "Definition", WordInformationType.DEFINITION);
        viewDefinitions.addView(container);
        return container;
    }

    private LinearLayout addSentenceView(WordSentence sentence) {
        LinearLayout sentenceLayout = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sentenceLayout.setLayoutParams(params);
        sentenceLayout.setOrientation(LinearLayout.HORIZONTAL);
        sentenceLayout.setTag(sentence == null ? 0 : sentence.getWordInformationId());
        sentenceLayout.setTag(R.string.tag_information_type, WordInformationType.SENTENCE);

        LinearLayout.LayoutParams editableParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        editableParams.topMargin = 5;
        editableParams.weight = 1;

        String[] editables = {"Sentence", "Translation"};
        for (int i = 0; i < editables.length; i++) {
            EditText editable = new EditText(this);
            editable.setBackgroundResource(R.drawable.edittext_editable);
            if (sentence != null) {
                String sentenceString = i == 0 ? sentence.getSentence() : sentence.getTranslation();
                editable.setText(sentenceString != null ? Html.fromHtml(sentenceString) : "");
            }
            editable.setHint(editables[i]);
            editable.setSingleLine();
            editable.setOnFocusChangeListener(new WordInformationEditTextFocusListener(this));
            editable.setImeOptions(EditorInfo.IME_ACTION_DONE);
            editable.setOnEditorActionListener(new WordInformationEditorActionListener());
            editable.setGravity(Gravity.TOP);
            // Enable text wrapping
            editable.setHorizontallyScrolling(false);
            editable.setMaxLines(Integer.MAX_VALUE);
            editable.setLayoutParams(editableParams);

            sentenceLayout.addView(editable);
        }

        addDeleteButton(sentenceLayout);

        viewSentences.addView(sentenceLayout);

        return sentenceLayout;
    }

    private LinearLayout addImageView(WordImage image) {
        LinearLayout imageContainer = new LinearLayout(this);


        imageContainer.setMinimumWidth(getResources().getDimensionPixelSize(R.dimen.word_view_image_min_width));
        imageContainer.setOrientation(LinearLayout.VERTICAL);
        imageContainer.setGravity(Gravity.BOTTOM);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.word_view_image_horizontal_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.word_view_image_horizontal_margin);
        params.topMargin = getResources().getDimensionPixelSize(R.dimen.word_view_image_top_margin);
        imageContainer.setLayoutParams(params);
        imageContainer.setTag(image.getWordInformationId());

        // Image

        ImageView imageView = new ImageView(this);
        //imageView.setWidth(getResources().getDimensionPixelSize(R.dimen.word_view_image_max_width));
        //imageView.setHeight(getResources().getDimensionPixelSize(R.dimen.word_view_image_max_height));

        Log.d("kioku-view", "img: " + image.getFileName());
        Picasso.with(this).load(Utils.mediaFile(this, image.getFileName())).resizeDimen(R.dimen.word_view_image_max_width, R.dimen.word_view_image_max_height)
                                                                            .centerInside()
                                                                            .into(imageView);

        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        imageParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        imageView.setLayoutParams(imageParams);

        imageContainer.addView(imageView);

        // Delete button

        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                                getResources().getDimensionPixelSize(R.dimen.word_view_delete_button_width));
        //deleteParams.topMargin = getResources().getDimensionPixelSize(R.dimen.word_view_image_delete_button_vertical_margin);
        //deleteParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.word_view_image_delete_button_vertical_margin);
        deleteParams.gravity = Gravity.CENTER;

        addDeleteButton(imageContainer, deleteParams);

        viewImages.addView(imageContainer);

        return imageContainer;
    }

    private void addDeleteButton(final LinearLayout layout) {
        //LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams((int)getResources().getDimension(R.dimen.word_view_delete_button_width),
        //        (int)getResources().getDimension(R.dimen.word_view_delete_button_width));
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.word_view_delete_button_width),
                                                                                ViewGroup.LayoutParams.MATCH_PARENT);


        //deleteParams.leftMargin = 20;
        deleteParams.rightMargin = 0;
        deleteParams.gravity = Gravity.CENTER;

        addDeleteButton(layout, deleteParams);
    }

    private void addDeleteButton(final LinearLayout layout, LinearLayout.LayoutParams deleteParams) {
        Button deleteButton = new Button(this);
        deleteButton.setText("X");
        deleteButton.setTypeface(null, Typeface.BOLD);
        deleteButton.setTextColor(Color.BLACK);
        deleteButton.setBackgroundColor(Color.TRANSPARENT);
        deleteButton.setTextSize(14);
        deleteButton.setPadding(0, 0, 0, 0);
        deleteButton.setLayoutParams(deleteParams);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFocusAll(); // clear focus to save any pending changes

                Integer wordInformationId = (Integer)layout.getTag();

                if (wordInformationId == null)
                    return; // already deleted (by the clearFocus)

                try {
                    WordInformation wordInformation = getHelper().getWordInformationDao().queryForId(wordInformationId);

                    requestDeleteWordInformation(wordInformation, layout);
                } catch (SQLException e) {
                    e.printStackTrace();

                }
            }
        });

        layout.addView(deleteButton);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (searchWord) {
            getMenuInflater().inflate(R.menu.menu_word_view_save, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_word_view, menu);

            MenuItem searchAddItem = menu.findItem(R.id.search_add_button);
            searchSubMenu = searchAddItem.getSubMenu();
            loadSearchTerms();
        }

        return true;
    }

    private void updateWordForm(WordInformation wordInformation, LinearLayout container) throws SQLException {
        if (wordInformation == null) {
            wordInformation = new WordInformation(WordInformationType.WORD_FORM, word);
        }

        EditText editableForm = (EditText) container.getChildAt(0);
        if (editableForm != null) {
            String formStr = editableForm.getText().toString();
            wordInformation.setInformationBytes(formStr.length() > 0 ? formStr.getBytes() : null);
        }

        boolean showFormMeta = word.getLanguage() == WordLanguage.JP.getValue();
        if (showFormMeta) {
            EditText editableFormMeta = (EditText) container.getChildAt(1);
            if (editableFormMeta != null) {
                String formMetaStr = editableFormMeta.getText().toString();
                wordInformation.setMetaInformationBytes(formMetaStr.length() > 0 ? formMetaStr.getBytes() : null);
            }
        }

        if ((wordInformation.getInformationBytes() == null || wordInformation.getInformationBytes().length == 0) &&
                (wordInformation.getMetaInformationBytes() == null || wordInformation.getMetaInformationBytes().length == 0)) {
            Log.d("kioku-edit", "deleting empty word form");
            getHelper().getWordInformationDao().delete(wordInformation);
        } else {
            Log.d("kioku-edit", "creating/updating word form");
            getHelper().getWordInformationDao().createOrUpdate(wordInformation);
            container.setTag(wordInformation.id);
        }
    }

    private void updateSingleLineInfo(WordInformation wordInformation, WordInformationType wordInformationType, LinearLayout container) throws SQLException {
        if (wordInformation == null) {
            wordInformation = new WordInformation(wordInformationType, word);
        }

        EditText editableText = (EditText)container.getChildAt(0);

        wordInformation.setInformationBytes(editableText.getText().toString().getBytes());

        if (wordInformation.getInformationBytes().length == 0) {
            Log.d("kioku-edit", "deleting empty " + wordInformationType);
            getHelper().getWordInformationDao().delete(wordInformation);
            //deleteWordInformation(wordInformation, container); // delete if input empty
        } else {
            Log.d("kioku-edit", "creating/updating " + wordInformationType);
            getHelper().getWordInformationDao().createOrUpdate(wordInformation);
            container.setTag(wordInformation.id);
        }
    }

    private void updateTranslation(WordInformation wordInformation, LinearLayout container) throws SQLException {
        updateSingleLineInfo(wordInformation, WordInformationType.TRANSLATION, container);
    }

    private void updateDefinition(WordInformation wordInformation, LinearLayout container) throws SQLException {
        updateSingleLineInfo(wordInformation, WordInformationType.DEFINITION, container);
    }

    private void updateSentence(WordInformation wordInformation, LinearLayout container) throws SQLException {
        if (wordInformation == null) {
            wordInformation = new WordInformation(WordInformationType.SENTENCE, word);
        }

        EditText editableSentence = (EditText)container.getChildAt(0);
        String sentenceString = editableSentence.getText().toString();
        wordInformation.setInformationBytes(sentenceString.length() > 0 ? sentenceString.getBytes() : null);

        EditText editableTranslation = (EditText)container.getChildAt(1);
        String translationString = editableTranslation.getText().toString();
        wordInformation.setMetaInformationBytes(translationString.length() > 0 ? translationString.getBytes() : null);

        if ((wordInformation.getInformationBytes() == null || wordInformation.getInformationBytes().length == 0) &&
                (wordInformation.getMetaInformationBytes() == null || wordInformation.getMetaInformationBytes().length == 0)) {
            Log.d("kioku-edit", "deleting empty sentence");
            getHelper().getWordInformationDao().delete(wordInformation);
        } else {
            Log.d("kioku-edit", "creating/updating sentence");
            getHelper().getWordInformationDao().createOrUpdate(wordInformation);
            container.setTag(wordInformation.id);
        }
    }

    private void updateEditText(LinearLayout layout) throws SQLException {
        Integer wordInformationId = (Integer)layout.getTag();
        WordInformation wordInformation = getHelper().getWordInformationDao().queryForId(wordInformationId);

        switch ((WordInformationType)layout.getTag(R.string.tag_information_type)) {
            case WORD_FORM:
                updateWordForm(wordInformation, layout);
                break;
            case TRANSLATION:
                updateTranslation(wordInformation, layout);
                break;
            case DEFINITION:
                updateDefinition(wordInformation, layout);
                break;
            case SENTENCE:
                updateSentence(wordInformation, layout);
                break;
        }

        onInfoChange();
    }

    private void saveWord() {
        try {
            TransactionManager.callInTransaction(getConnectionSource(), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    // Update word forms
                    for (int i = 0; i < viewWordForms.getChildCount(); i++) {
                        LinearLayout formLayout = (LinearLayout)viewWordForms.getChildAt(i);
                        updateEditText(formLayout);
                    }

                    // Update translations
                    for (int i = 0; i < viewTranslations.getChildCount(); i++) {
                        LinearLayout editableTranslationLayout = (LinearLayout)viewTranslations.getChildAt(i);
                        updateEditText(editableTranslationLayout);
                    }

                    // Update definitions
                    for (int i = 0; i < viewDefinitions.getChildCount(); i++) {
                        LinearLayout editableDefinitionLayout = (LinearLayout)viewDefinitions.getChildAt(i);
                        updateEditText(editableDefinitionLayout);
                    }

                    // Update sentences
                    for (int i = 0; i < viewSentences.getChildCount(); i++) {
                        LinearLayout editableSentenceLayout = (LinearLayout)viewSentences.getChildAt(i);
                        updateEditText(editableSentenceLayout);
                    }

                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();

            Toast.makeText(this, "Error editing word", Toast.LENGTH_SHORT).show();
        }

        if (searchWord) {
            if (newWord) {
                // Finish activity and return OK to search
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                // Finish search results activity
                SearchResultsActivity.activeActivity.setResult(Activity.RESULT_OK);
                SearchResultsActivity.activeActivity.finish();

                // Return to view mode
                searchWord = false;
                invalidateOptionsMenu();
            }
        }
    }

    public void requestDeleteWord() {
        final Context context = this;

        new AlertDialog.Builder(this)
                .setTitle("Delete word")
                .setMessage("Are you sure you want to delete this word?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Clear saved word
                        try {
                            // Remove word from DB
                            Dao<Word, Integer> wordDao = getHelper().getWordDao();

                            if (KiokuServerClient.isLoggedIn(context)) { // Markers are only needed for logged in users for sync
                                Word deletedMarker = (Word) word.getDeletedMarker();
                                if (deletedMarker != null)
                                    wordDao.create(deletedMarker);
                            }

                            List<WordImage> wordImages = word.getImages(getHelper());

                            // Delete the word; cascade deletion also deletes all associated word information
                            wordDao.delete(word);

                            // Delete all files this word used and are now unused
                            for (WordImage image : wordImages)
                                deleteImageIfUnused(context, image.getFileName());

                            // Return to browse
                            finish();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }).create().show();
    }

    private void requestDeleteWordInformation(final WordInformation wordInformation, final LinearLayout layout) {
        Log.d("kioku-edit", "request delete info " + wordInformation);

        final Activity activity = this;

        if (wordInformation == null || ((wordInformation.getInformationBytes() == null || wordInformation.getInformationBytes().length == 0) &&
                (wordInformation.getMetaInformationBytes() == null || wordInformation.getMetaInformationBytes().length == 0))) {
            // Delete empty entries without prompt
            deleteWordInformation(wordInformation, layout);
            clearFocusAll();
        } else{
            new AlertDialog.Builder(this)
                    .setTitle("Delete word information")
                    .setMessage("Are you sure you want to delete this information?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteWordInformation(wordInformation, layout);
                            Utils.hideKeyboard(activity);
                        }
                    }).create().show();
        }
    }

    private void deleteWordInformation(WordInformation wordInformation, LinearLayout layout) {
        try {
            if (wordInformation != null) {
                // Remove word information from DB
                Dao<WordInformation, Integer> wordInformationDao = getHelper().getWordInformationDao();

                if (KiokuServerClient.isLoggedIn(this)) { // Markers are only needed for logged in users for sync
                    WordInformation deletedMarker = (WordInformation)wordInformation.getDeletedMarker();
                    if (deletedMarker != null)
                        wordInformationDao.create(deletedMarker);
                }

                wordInformationDao.delete(wordInformation);


                if (wordInformation.getInformationType() == WordInformationType.IMAGE) {
                    deleteImageIfUnused(this, new String(wordInformation.getInformationBytes()));
                }

                // Mark as deleted
                layout.setTag(null);

                onInfoChange();
            }

            ((ViewGroup) layout.getParent()).removeView(layout);

            clearFocusAll();
            Utils.hideKeyboard(this);
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error deleting word information", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteImageIfUnused(Context context, String fileName) throws SQLException {
        // Check image isn't used anymore
        boolean imageUsed = getHelper().getWordInformationDao().queryBuilder().where().eq("informationType", WordInformationType.IMAGE)
                .and()
                .raw("cast(informationBytes AS TEXT) = ?",
                        new SelectArg(SqlType.LONG_STRING, fileName))
                .countOf() > 0;

        if (!imageUsed) {
            File file = Utils.mediaFile(context, fileName);
            if (file.delete())
                Log.d("kioku-del", "deleted " + fileName);
            else
                Log.d("kioku-del", "error deleting " + fileName);
        } else {
            Log.d("kioku-del", "image is used elsewhere: " + fileName);
        }
    }

    private class WordInformationEditTextFocusListener implements View.OnFocusChangeListener {
        private Activity activity;

        public WordInformationEditTextFocusListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            Log.d("kioku-edit", "[" + v + "] onFocusChange: " + hasFocus + "," + ((LinearLayout)v.getParent()).getTag());
            Log.d("kioku-edit", "currentFocus: " + getCurrentFocus());

            if (!hasFocus) {
                //clearFocusAll();

                LinearLayout layout = (LinearLayout)v.getParent();
                if (layout.getTag() != null) { // deleted words have null tag
                    try {
                        updateEditText(layout);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        Toast.makeText(activity, "Error updating word information", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Utils.showKeyboard(activity);
            }
        }
    }

    /**
     * Listens for IME_ACTION_DONE on EditText and unfocuses when received.
     * Once the user saves their edit, the EditText should not be focused.
     */
    private class WordInformationEditorActionListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                clearFocusAll();
            }

            return false;
        }
    }
}