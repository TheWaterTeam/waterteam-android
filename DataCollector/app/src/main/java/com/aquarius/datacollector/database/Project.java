package com.aquarius.datacollector.database;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by matthewxi on 11/13/17.
 */

public class Project extends RealmObject {

    public static final String DEFAULT_PROJECT_NAME = "First Project";

    private String UUID;

    String name;
    boolean defaultProject;
    double westernLongitude;
    double northernLatitude;
    double easternLongitude;
    double southernLatitude;

    public RealmList<DataLogger> dataLoggers; // Declare one-to-many relationships


    public Project() {
        UUID = java.util.UUID.randomUUID().toString();
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefaultProject() {
        return defaultProject;
    }

    public void setDefaultProject(boolean defaultProject) {
        this.defaultProject = defaultProject;
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
