package me.cthorne.kioku.languages;

import me.cthorne.kioku.words.WordLanguage;

/**
 * Created by chris on 10/02/16.
 */
public class SelectableWordLanguage {
    private WordLanguage language;
    private boolean selected;

    public SelectableWordLanguage(WordLanguage language) {
        this.language = language;
        this.selected = false;
    }

    public WordLanguage getLanguage() {
        return language;
    }

    public void setLanguage(WordLanguage language) {
        this.language = language;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return language.toString();
    }

}
