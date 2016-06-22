package me.cthorne.kioku.infosources;

/**
 * Created by chris on 24/01/16.
 */
public class SelectableWordInformationSource {

    private WordInformationSource source;
    private boolean selected;
    private boolean recommended;
    private boolean disabled;

    public SelectableWordInformationSource(String name, String title, String url, boolean recommended, boolean disabled) {
        this.source = new WordInformationSource(name, title, url);
        this.selected = false;
        this.recommended = recommended;
        this.disabled = disabled;
    }

    public WordInformationSource getSource() {
        return source;
    }

    public void setSource(WordInformationSource source) {
        this.source = source;
    }


    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public String toString() {
        return source.toString();
    }
}
