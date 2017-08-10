package com.aquarius.datacollector.database;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by matthewxi on 8/8/17.
 */

public class DataLog extends RealmObject {
    @PrimaryKey
    private long id;
    private long deviceId;
    private String filePath;
    private boolean uploaded;
    private Date dateRetreived;
    private Date dateUploaded;

    public long getId() {
        return id;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public Date getDateRetreived() {
        return dateRetreived;
    }

    public void setDateRetreived(Date dateRetreived) {
        this.dateRetreived = dateRetreived;
    }

    public Date getDateUploaded() {
        return dateUploaded;
    }

    public void setDateUploaded(Date dateUploaded) {
        this.dateUploaded = dateUploaded;
    }
}
