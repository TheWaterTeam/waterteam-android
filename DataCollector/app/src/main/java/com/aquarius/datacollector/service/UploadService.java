package com.aquarius.datacollector.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.aquarius.datacollector.api.Api;
import com.aquarius.datacollector.api.ErrorMessageException;
import com.aquarius.datacollector.api.responses.FileResponse;
import com.aquarius.datacollector.database.DataLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by matthewxi on 8/9/17.
 */

public class UploadService extends IntentService {

    public static final String TAG = UploadService.class.getSimpleName();
    public static final String ACTION_UPLOAD_ALL = "ACTION_UPLOAD_ALL";

    public static void startActionUpload(Context context) {
        Log.d(TAG, "startActionUpload");
        Intent intent = new Intent(context, UploadService.class);
        intent.setAction(ACTION_UPLOAD_ALL);
        context.startService(intent);

    }

    private Realm realm;


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public UploadService() {
        super("UploadService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent");
        Realm.init(this);
        realm = Realm.getDefaultInstance();

        // Upload All Not Currently Uploaded
        // Since this is an IntentService we are already on a separate thread
        RealmQuery<DataLog> query = realm.where(DataLog.class).equalTo("uploaded", false);
        RealmResults<DataLog> results = query.findAll();
        for(DataLog dataLog : results){
            File file = new File(dataLog.getFilePath());
            Log.d(TAG, "Uploading " + dataLog.getFilePath());
            try {
                FileResponse response = Api.getInstance().uploadTimeseries(getApplicationContext(), file);
                if(true){
                    realm.beginTransaction();
                    dataLog.setUploaded(true);
                    realm.commitTransaction();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ErrorMessageException e) {
                e.printStackTrace();
            }
        }
        realm.close();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
