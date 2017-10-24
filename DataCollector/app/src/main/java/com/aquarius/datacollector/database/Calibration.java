package com.aquarius.datacollector.database;

import io.realm.RealmObject;

/**
 * Created by matthewxi on 10/23/17.
 */

public class Calibration extends RealmObject {
    private String type;
    private double a;
    private double b;
    private double c;
    private double d;
}
