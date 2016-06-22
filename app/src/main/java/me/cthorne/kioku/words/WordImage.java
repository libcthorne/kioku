package me.cthorne.kioku.words;

/**
 * Created by chris on 07/02/16.
 */
public class WordImage {
    private String fileName;
    private int wordInformationId;

    public WordImage(WordInformation information) {
        this.fileName = new String(information.getInformationBytes());
        this.wordInformationId = information.id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getWordInformationId() {
        return wordInformationId;
    }

    public void setWordInformationId(int wordInformationId) {
        this.wordInformationId = wordInformationId;
    }
}
