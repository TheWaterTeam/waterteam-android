package com.aquarius.datacollector.activities.fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aquarius.datacollector.R;
import com.aquarius.datacollector.application.Preferences;
import com.aquarius.datacollector.control.Control;
import com.aquarius.datacollector.control.ControlListener;
import com.aquarius.datacollector.database.DataLog;
import com.aquarius.datacollector.database.DataLogger;
import com.aquarius.datacollector.database.Project;
import com.aquarius.datacollector.service.UsbService;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import io.realm.Realm;

/**
 * Created by matthewxi on 11/8/17.
 */

public class SerialDownloadFragment extends Fragment implements ControlListener {

    private static String TAG = "SerialConsoleFragment";

    private String connectedProbeUUID;
    private long downloadRequestTime;


    /*
 * Notifications from UsbService will be received here.
 */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendCommand(">WT_OPEN<");
                        }
                    }, 3000);

                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_ATTACHED:
                    //Toast.makeText(context, "USB Device Connected", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private UsbService usbService;
    private TextView display;
    private EditText editText;
    private TextView dataLoggerIdTextView;
    private TextView lastDownloadDateTextView;

    private SerialDownloadFragment.MyHandler mHandler;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    private Control control;
    private Realm realm;
    private DataLogger connectedDataLogger;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new SerialDownloadFragment.MyHandler(this);

        control = new Control(getContext());
        control.setListener(this);

        Realm.init(getContext());
        realm = Realm.getDefaultInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.serial_download_fragment, container, false);

        display = (TextView) view.findViewById(R.id.textView1);
        display.setMovementMethod(new ScrollingMovementMethod());

        /*
        editText = (EditText) view.findViewById(R.id.editText1);
        editText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    send();
                    return true;
                }
                return false;
            }
        });
        */

        dataLoggerIdTextView = (TextView) view.findViewById(R.id.dataloggerIdTextView);
        lastDownloadDateTextView = (TextView) view.findViewById(R.id.lastDownloadDateTextView);

        /*
        Button sendButton = (Button) view.findViewById(R.id.buttonSend);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editText.getText().toString().equals("")) {
                    send();
                }
            }
        });
        */

        Button requestDownloadButton = (Button) view.findViewById(R.id.buttonRequestDownload);
        requestDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SerialDownloadFragment.this.downloadRequestTime = System.currentTimeMillis() / 1000L;

                if(connectedDataLogger != null) {
                    String command = ">WT_DOWNLOAD:" + String.valueOf(connectedDataLogger.getLastDownloadedFileDate()) + "<";
                    sendCommand(command);
                } else {
                    display.append("No datalogger connected\n");
                }

            }
        });

        Button setRTCButton = (Button) view.findViewById(R.id.setRTC);
        setRTCButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                long unixTime = System.currentTimeMillis() / 1000L;
                if(connectedDataLogger != null) {
                    String command = ">WT_SET_RTC:" + String.valueOf(unixTime) + "<";
                    sendCommand(command);
                } else {
                    display.append("No datalogger connected\n");
                }

            }
        });

        Button deployButton = (Button) view.findViewById(R.id.deployButton);
        deployButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                long unixTime = System.currentTimeMillis() / 1000L;
                if(connectedDataLogger != null) {
                    long dv = unixTime*1000;// its need to be in milisecond
                    Date df = new java.util.Date(dv);
                    String deploymentIdentifier = new SimpleDateFormat("yyyyMMddhhmmss").format(df);
                    deploymentIdentifier = "SITENAME_" + deploymentIdentifier;
                    String command = ">WT_DEPLOY:" + deploymentIdentifier + "<";
                    sendCommand(command);
                } else {
                    display.append("No datalogger connected\n");
                }

            }
        });


        return view;
    }

    public void send(){
        if (!editText.getText().toString().equals("")) {
            String data = editText.getText().toString();
            if (usbService != null) { // if UsbService was correctly binded, Send data
                usbService.write(data.getBytes());
            }
        }
    }

    public void sendCommand(String command){
        Log.d(TAG, "Send Command");
        display.append("Command:" + command + "\n");
        if (usbService != null) { // if UsbService was correctly binded, Send data
            usbService.write(command.getBytes());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mUsbReceiver);
        getActivity().unbindService(usbConnection);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }


    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(getActivity(), service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            getActivity().startService(startService);
        }
        Intent bindingIntent = new Intent(getActivity(), service);
        getActivity().bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        filter.addAction(UsbService.ACTION_USB_ATTACHED);
        getActivity().registerReceiver(mUsbReceiver, filter);
    }

    private void updateConnectedDatalogger(String uuid){
        connectedProbeUUID = uuid;
        display.append("Looking for Datalogger object " + connectedProbeUUID);
        connectedDataLogger = realm.where(DataLogger.class).equalTo("UUID", connectedProbeUUID).findFirst();
        if(connectedDataLogger == null) {
            display.append("Created new Datalogger object");
            realm.beginTransaction();
            connectedDataLogger = realm.createObject(DataLogger.class);
            connectedDataLogger.setUUID(uuid);
            Project selectedProject = Preferences.getSelectedProject(getContext(), realm);
            selectedProject.dataLoggers.add(connectedDataLogger);
            realm.commitTransaction();
        }

        updateUI();
    }

    private void updateUI(){
        dataLoggerIdTextView.setText("Current Datalogger UUID: " + connectedDataLogger.getUUID());
        lastDownloadDateTextView.setText("Last Download Date: " + connectedDataLogger.getLastDownloadDate());
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<SerialDownloadFragment> mFragment;

        public MyHandler(SerialDownloadFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    //mFragment.get().display.append("Data: " + data);
                    try {
                        Log.d(TAG, "Received Data "+ data);
                        mFragment.get().control.receivedData(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // We should probably reset the connction / switch back to control mode here.
                    }
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mFragment.get().getActivity(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mFragment.get().getActivity(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }


    @Override
    public void processCommand(String command){
        display.append("Processing Command: " + command + " -- \n\n");

        // If the command is AQ_TRANSFER_READY
        // then go into file transfer mode mode, writing all the data sent out to a text file
        if(command.contains("WT_IDENTIFY")) {
            String deviceUUID = command.substring(command.indexOf(":")+1);
            display.append("GOT DEVICE UUID " + deviceUUID + "\n\n");
            updateConnectedDatalogger(deviceUUID);


        } else if(command.contains("WT_READY")){
            // we are in file transfer mode
            // switch to file transfer mode
            try {
                control.setMode(Control.FILE_TRANSFER_MODE);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // send ACK
            usbService.write(Control.ACK.getBytes());
            display.append("SEND " + Control.ACK + "\n\n");

        } else if (command.contains("WT_COMPLETE")){
            if(command.contains(":")){
                String lastDownloadFromDatalogger = command.substring(command.indexOf(":")+1);
                // TODO store this lastDownloadFromDatalogger into the database on this device
                // which should also be updated from the server on syncs for this project.
                display.append("GOT LAST DOWNLOAD DATE " + lastDownloadFromDatalogger+ "\n\n");

                realm.beginTransaction();
                long dv = Long.valueOf(this.downloadRequestTime)*1000;// its need to be in milisecond
                Date df = new java.util.Date(dv);
                String downloadDateString = new SimpleDateFormat("MM dd, yyyy hh:mma").format(df);
                connectedDataLogger.setLastDownloadedFileDate(lastDownloadFromDatalogger);
                connectedDataLogger.setLastDownloadDate(String.valueOf(downloadDateString));
                realm.commitTransaction();

                updateUI();
            }
        } else if (command.contains("WT_TIMESTAMP")){
            if(command.contains(":")) {
                String timestampFromDatalogger = command.substring(command.indexOf(":") + 1);
                long timestamp = Long.parseLong(timestampFromDatalogger);
                long dv = Long.valueOf(timestamp)*1000;// its need to be in milisecond
                Date df = new java.util.Date(dv);
                String dateString = new SimpleDateFormat("MM dd, yyyy hh:mma").format(df);
                display.append("CURRENT DATALOGGER TIME: " + dateString);
            }
        } else {
            display.append(command + "\n");
        }
    }

    @Override
    public void fileTransfered(File fileTransferStorage) {
        // put file into the database so we can upload it later
        // this is where we need to at least now the device_id of this device

        Number lastId = realm.where(DataLog.class).max("id");
        int nextID = 1;
        if(lastId != null) {
            nextID = (realm.where(DataLog.class).max("id").intValue() + 1); // TODO: not great
        }
        realm.beginTransaction();
        DataLog dataLog = realm.createObject(DataLog.class, nextID);
        dataLog.setUploaded(false);
        dataLog.setProbeUUID(connectedProbeUUID); // TODO: Hard coded device Id
        dataLog.setFilePath(fileTransferStorage.getPath());
        dataLog.setDateRetreived(new Date());
        realm.commitTransaction();
    }


}
