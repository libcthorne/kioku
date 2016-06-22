package me.cthorne.kioku.words;

import java.util.Locale;

/**
 * Created by chris on 23/01/16.
 */
public enum WordLanguage {
    JP(1), // Japanese
    EN(2); // English

    private final int value;
    private int wordFormInformationHint;
    private Locale locale;

    WordLanguage(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        switch (this) {
            case JP: return "Japanese";
            case EN: return "English";
            default: return super.toString();
        }
    }

    public static WordLanguage fromInt(int value) {
        for (WordLanguage language : WordLanguage.values()) {
            if (language.getValue() == value)
                return language;
        }

        return null;
    }

    public String getWordFormInformationHint() {
        switch (this) {
            case JP: return "Kanji";
            //case EN: return "Word";
            default: return "";
        }
    }

    public String getWordFormMetaInformationHint() {
        switch (this) {
            case JP: return "Kana";
            //case EN: return "";
            default: return "";
        }
    }

    public Locale getLocale() {
        switch (this) {
            case JP: return Locale.JAPAN;
            //case EN: return Locale.ENGLISH;
            default: return Locale.ENGLISH;
        }
    }
}
