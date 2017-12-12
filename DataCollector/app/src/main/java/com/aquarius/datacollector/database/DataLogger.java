package com.aquarius.datacollector.database;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

/**
 * Created by matthewxi on 10/23/17.
 */

public class DataLogger extends RealmObject {

    @SerializedName("uniqueIdentifier")
    private String UUID;

    private String lastDownloadDate;
    private String description;

    private double latitude;
    private double longitude;

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
}
