package me.cthorne.kioku.words;

/**
 * Created by chris on 06/02/16.
 */
public class WordDefinition {
    private String text;
    private int wordInformationId;

    public WordDefinition(WordInformation information) {
        if (information.getInformationBytes() != null)
            this.text = new String(information.getInformationBytes());
        else
            this.text = new String();

        this.wordInformationId = information.id;
    }

    public int getWordInformationId() {
        return wordInformationId;
    }

    public void setWordInformationId(int wordInformationId) {
        this.wordInformationId = wordInformationId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
