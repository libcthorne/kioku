package me.cthorne.kioku.sync;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

/**
 * Created by chris on 30/01/16.
 */
public class SyncItem {
    private Dao dao;
    private QueryBuilder entitySelector;
    private String entityName;

    public SyncItem(Dao dao, QueryBuilder entitySelector, String entityName) {
        this.dao = dao;
        this.entitySelector = entitySelector;
        this.entityName = entityName;
    }

    public Dao getDao() {
        return dao;
    }

    public QueryBuilder getEntitySelector() {
        return entitySelector;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getEntityNameForUser() {
        return entityName.replace("_", " ") + "s";
    }
}