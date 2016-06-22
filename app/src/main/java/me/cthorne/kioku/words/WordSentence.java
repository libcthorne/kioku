package me.cthorne.kioku.words;

/**
 * Created by chris on 17/02/16.
 */
public class WordSentence {
    private String sentenceText;
    private String translationText;
    private int wordInformationId;

    public WordSentence(WordInformation information) {
        if (information.getInformationBytes() != null)
            this.sentenceText = new String(information.getInformationBytes());

        if (information.getMetaInformationBytes() != null)
            this.translationText = new String(information.getMetaInformationBytes());

        this.wordInformationId = information.id;
    }

    public int getWordInformationId() {
        return wordInformationId;
    }

    public void setWordInformationId(int wordInformationId) {
        this.wordInformationId = wordInformationId;
    }

    public String getSentence() {
        return sentenceText;
    }

    public void setSentence(String text) {
        this.sentenceText = text;
    }

    public String getTranslation() {
        return translationText;
    }

    public void setTranslation(String text) {
        this.translationText = text;
    }


}
