package com.aquarius.datacollector.database;

import io.realm.RealmObject;

/**
 * Created by matthewxi on 10/23/17.
 */

public class DataLogger extends RealmObject {
    private String UUID;
    private String lastDownloadDate;

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getLastDownloadDate() {
        if(lastDownloadDate != null) {
            return lastDownloadDate;
        } else {
            return "0000000000";
        }
    }

    public void setLastDownloadDate(String lastDownloadDate) {
        this.lastDownloadDate = lastDownloadDate;
    }
}
