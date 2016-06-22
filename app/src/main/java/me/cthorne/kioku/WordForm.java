package me.cthorne.kioku;

import me.cthorne.kioku.words.WordInformation;

/**
 * Created by chris on 07/02/16.
 */
public class WordForm {

    private String form;
    private String formMeta; // info about the form (e.g. kana reading for kanji)
    private int wordInformationId;

    public WordForm(WordInformation information) {
        if (information.getInformationBytes() != null)
            this.form = new String(information.getInformationBytes());

        if (information.getMetaInformationBytes() != null)
            this.formMeta = new String(information.getMetaInformationBytes());

        this.wordInformationId = information.id;
    }

    public String getForm() {
        return form;
    }

    public String getFormMeta() {
        return formMeta;
    }

    public int getWordInformationId() {
        return wordInformationId;
    }
}
