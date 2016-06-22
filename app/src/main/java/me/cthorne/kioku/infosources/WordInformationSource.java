package me.cthorne.kioku.infosources;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by chris on 23/01/16.
 */
@DatabaseTable(tableName = "word_information_sources")
public class WordInformationSource {

    // Name of source (e.g. google-images)
    @DatabaseField(id = true)
    private String name;

    // Title of source (e.g. Google Images)
    @DatabaseField(canBeNull = false)
    private String title;

    // Version of local copy
    @DatabaseField(canBeNull = false)
    private int version;

    // URL of source (e.g. http://www.google.co.uk/?q=[WORD])
    // [WORD] is automatically replaced with the user's search query
    @DatabaseField(canBeNull = false)
    private String url;

    // JavaScript for select mode
    @DatabaseField(canBeNull = false, dataType = DataType.LONG_STRING)
    private String selectJS;

    // JavaScript for saving word information
    @DatabaseField(canBeNull = false, dataType = DataType.LONG_STRING)
    private String saveJS;

    public WordInformationSource() {
        // ORMLite constructor
    }

    public WordInformationSource(String name, String title, String url) {
        this.name = name;
        this.title = title;
        this.url = url;
    }

    public WordInformationSource(String name, int version, String title, String url, String selectJS, String saveJS) {
        this.name = name;
        this.title = title;
        this.version = version;
        this.url = url;
        this.selectJS = selectJS;
        this.saveJS = saveJS;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        if (title == null) {
            if (getVersion() == 0)
                return name + " [pending download]";
            else
                return name;
        }

        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getUrl(String searchString) {
        return url.replace("[WORD]", searchString);
    }

    public int getVersion() {
        return version;
    }

    /**
     * Gets only the domain of the URL.
     * @return
     */
    public String getUrlDomain() {
        String domainUrl = new String(url);

        // TODO

        return domainUrl;
    }

    public String getSelectJS() {
        return selectJS;
    }

    public String getSaveJS() {
        return saveJS;
    }

    @Override
    public String toString() {
        return getTitle();
    }
}