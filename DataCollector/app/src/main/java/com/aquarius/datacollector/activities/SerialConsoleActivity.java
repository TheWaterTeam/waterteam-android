package com.aquarius.datacollector.activities;

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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aquarius.datacollector.R;
import com.aquarius.datacollector.control.Control;
import com.aquarius.datacollector.control.ControlListener;
import com.aquarius.datacollector.database.DataLog;
import com.aquarius.datacollector.service.UsbService;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Set;

import io.realm.Realm;

public class SerialConsoleActivity extends AppCompatActivity implements ControlListener {

    private static String TAG = "SerialConsoleActivity";

    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
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
            }
        }
    };
    private UsbService usbService;
    private TextView display;
    private EditText editText;
    private MyHandler mHandler;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serial_console);

        mHandler = new MyHandler(this);

        display = (TextView) findViewById(R.id.textView1);
        editText = (EditText) findViewById(R.id.editText1);
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

        Button sendButton = (Button) findViewById(R.id.buttonSend);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editText.getText().toString().equals("")) {
                    send();
                }
            }
        });
        Button requestDownloadButton = (Button) findViewById(R.id.buttonRequestDownload);
        requestDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String data = ">WT_REQUEST_DOWNLOAD<";
                if (usbService != null) { // if UsbService was correctly binded, Send data
                    usbService.write(data.getBytes());
                }

           }
        });


        control = new Control(this);
        control.setListener(this);

        Realm.init(this);
        realm = Realm.getDefaultInstance();
    }

    public void send(){
        if (!editText.getText().toString().equals("")) {
            String data = editText.getText().toString();
            if (usbService != null) { // if UsbService was correctly binded, Send data
                usbService.write(data.getBytes());
            }
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
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }


    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<SerialConsoleActivity> mActivity;

        public MyHandler(SerialConsoleActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    mActivity.get().display.append(data);
                    try {
                        Log.d(TAG, "Received Data "+ data);
                        mActivity.get().control.receivedData(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // We should probably reset the connction / switch back to control mode here.
                    }
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }


    @Override
    public void processCommand(String command){
        display.append("Processing Command: " + command + " -- \n\n");

        // If the command is AQ_TRANSFER_READY
        // then go into file transfer mode mode, writing all the data sent out to a text file
        if(command.equals("WT_TRANSFER_READY")){
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

        }
    }

    @Override
    public void fileTransfered(File fileTransferStorage) {
        // put file into the database so we can upload it later
        // this is where we need to at least now the device_id of this device
        // is this some unique USB identifier? NO
        // so this is why we need the RFID chip, but many Androids won't have RFID..

        Number lastId = realm.where(DataLog.class).max("id");
        int nextID = 1;
        if(lastId != null) {
            nextID = (realm.where(DataLog.class).max("id").intValue() + 1); // TODO: not great
        }
        realm.beginTransaction();
        DataLog dataLog = realm.createObject(DataLog.class, nextID);
        dataLog.setUploaded(false);
        dataLog.setDeviceId(1); // TODO: Hard coded device Id
        dataLog.setFilePath(fileTransferStorage.getPath());
        dataLog.setDateRetreived(new Date());
        realm.commitTransaction();
    }

}