package com.aquarius.datacollector.application;

import android.content.Context;
import android.content.SharedPreferences;

import com.aquarius.datacollector.R;
import com.aquarius.datacollector.database.Project;

import io.realm.Realm;
import io.realm.RealmQuery;

/**
 * Created by zaven on 2/20/18.
 */

public class Preferences {
    public static final String SELECTED_PROJECT_UUID_KEY = "SELECTED_PROJECT_UUID_KEY";

    public static SharedPreferences getSharedPreferences(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref;
    }

    public static Project getSelectedProject (Context context, Realm realm) {
        SharedPreferences sharedPreferences = Preferences.getSharedPreferences(context);
        String selectedProjectUUID = sharedPreferences.getString(Preferences.SELECTED_PROJECT_UUID_KEY, null);

        Project selectedProject = null;
        if(selectedProjectUUID != null) {
            selectedProject = realm.where(Project.class).equalTo("UUID", selectedProjectUUID).findFirst();
        }
        if(selectedProject == null){
            selectedProject = Preferences.getDefaultProject(realm);
        }
        return selectedProject;
    }

    public static void setSelectedProject (Context context, Project project) {
        SharedPreferences.Editor sharedPreferencesEditor = Preferences.getSharedPreferences(context).edit();
        sharedPreferencesEditor.putString(Preferences.SELECTED_PROJECT_UUID_KEY, project.getUUID());
        sharedPreferencesEditor.commit();
    }

    public static Project getDefaultProject (Realm realm) {
        // Initialize the default project
        Project defaultProject = realm.where(Project.class).equalTo("defaultProject", true).findFirst();
        if (defaultProject == null) {
            realm.beginTransaction();
            defaultProject = realm.createObject(Project.class);
            defaultProject.setName(Project.DEFAULT_PROJECT_NAME);
            defaultProject.setDefaultProject(true);
            realm.commitTransaction();
            defaultProject = realm.where(Project.class).equalTo("defaultProject", true).findFirst();
        }
        return defaultProject;
    }
}
