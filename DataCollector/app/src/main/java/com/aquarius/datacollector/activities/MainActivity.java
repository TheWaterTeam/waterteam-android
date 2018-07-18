package com.aquarius.datacollector.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.aquarius.datacollector.BuildConfig;
import com.aquarius.datacollector.R;
import com.aquarius.datacollector.activities.fragments.DataFilesFragment;
import com.aquarius.datacollector.activities.fragments.ProjectFragment;
import com.aquarius.datacollector.activities.fragments.SerialDownloadFragment;

import io.realm.Realm;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private Realm realm;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_map: {
                    ProjectFragment fragment = new ProjectFragment();

                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.contentFrame, fragment);
                    transaction.commit();
                    fm.executePendingTransactions();
                }
                    return true;
                case R.id.navigation_data: {
                    DataFilesFragment fragment = new DataFilesFragment();

                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.contentFrame, fragment);
                    transaction.commit();
                    fm.executePendingTransactions();
                }
                    return true;
                case R.id.navigation_settings: {
                    //Intent intent = new Intent(MainActivity.this, SerialConsoleActivity.class);
                    //startActivity(intent);
                    SerialDownloadFragment fragment = new SerialDownloadFragment();

                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.contentFrame, fragment);
                    transaction.commit();
                    fm.executePendingTransactions();
                }
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Fabric.with(this, new Crashlytics());

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            //Timber.plant(new CrashReportingTree());
        }

        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Realm.init(this);
        realm = Realm.getDefaultInstance();


        // Go to the project fragment
        ProjectFragment fragment = new ProjectFragment();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.contentFrame, fragment);
        transaction.commit();

    }


    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
