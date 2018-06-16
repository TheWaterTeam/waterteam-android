package com.aquarius.datacollector.database;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

/**
 * Created by matthewxi on 10/23/17.
 */

public class DataLogger extends RealmObject {

    @SerializedName("uniqueIdentifier")
    private String UUID;

    private String lastDownloadedFileDate;
    private String lastDownloadDate;
    private String description;

    private double latitude;
    private double longitude;

    @LinkingObjects("dataLoggers")
    private final RealmResults<Project> project;

    public DataLogger() {
        project = null;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getLastDownloadedFileDate() {
        if(lastDownloadedFileDate != null) {
            return lastDownloadedFileDate;
        } else {
            return "0000000000";
        }
    }

    public void setLastDownloadedFileDate(String lastDownloadedFileDate) {
        this.lastDownloadedFileDate = lastDownloadedFileDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getLastDownloadDate() {
        if(this.lastDownloadDate != null) {
            return this.lastDownloadDate;
        } else {
            return "0000000000";
        }    }

    public void setLastDownloadDate(String lastDownloadDate) {
        this.lastDownloadDate = lastDownloadDate;
    }
}
