package com.aquarius.datacollector.database;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by matthewxi on 11/13/17.
 */

public class Project extends RealmObject {
    @PrimaryKey
    int projectId;
    String name;
    double westernLongitude;
    double northernLatitude;
    double easternLongitude;
    double southernLatitude;

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getWesternLongitude() {
        return westernLongitude;
    }

    public void setWesternLongitude(double westernLongitude) {
        this.westernLongitude = westernLongitude;
    }

    public double getNorthernLatitude() {
        return northernLatitude;
    }

    public void setNorthernLatitude(double northernLatitude) {
        this.northernLatitude = northernLatitude;
    }

    public double getEasternLongitude() {
        return easternLongitude;
    }

    public void setEasternLongitude(double easternLongitude) {
        this.easternLongitude = easternLongitude;
    }

    public double getSouthernLatitude() {
        return southernLatitude;
    }

    public void setSouthernLatitude(double southernLatitude) {
        this.southernLatitude = southernLatitude;
    }
}
