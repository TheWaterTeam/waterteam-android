package com.aquarius.datacollector.activities.fragments


import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.support.v4.app.Fragment
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.beepiz.bluetooth.gattcoroutines.experimental.GattConnection
import com.beepiz.bluetooth.gattcoroutines.experimental.extensions.get

import com.aquarius.datacollector.R
import com.aquarius.datacollector.application.Preferences
import com.aquarius.datacollector.control.Control
import com.aquarius.datacollector.control.ControlListener
import com.aquarius.datacollector.database.DataLog
import com.aquarius.datacollector.database.DataLogger
import com.aquarius.datacollector.database.Project
import com.aquarius.datacollector.service.UsbService

import splitties.systemservices.bluetoothManager
import timber.log.Timber


import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat

import io.realm.Realm
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.consumeEach
import java.util.*

/**
 * Created by matthewxi on 11/8/17.
 */

class SerialDownloadFragment2 : Fragment(), ControlListener {

    private var connectedProbeUUID: String? = null
    private var downloadRequestTime: Long = 0


    /*
 * Notifications from UsbService will be received here.
 */

    // TODO: check for existing connection
    // http://blog.blecentral.com/2015/10/01/handling-usb-connections-in-android/

    private val mUsbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val handler = Handler()

            when (intent.action) {
                UsbService.ACTION_USB_READY // USB PERMISSION GRANTED
                -> {
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show()

                    handler.postDelayed({ display!!.append("ACTION_USB_READY\n") }, 200)

                    handler.postDelayed({ sendCommand(">WT_OPEN<") }, 3000)  // This delay is
                }
                UsbService.ACTION_USB_PERMISSION_GRANTED // USB PERMISSION GRANTED
                -> {
                    Toast.makeText(context, "USB Permission Granted", Toast.LENGTH_SHORT).show()

                    handler.postDelayed({
                        display!!.append("ACTION_USB_PERMISSION_GRANTED\n")
                        display!!.append("VID " + intent.getIntExtra("vid", 0))
                        display!!.append("PID " + intent.getIntExtra("pid", 0))
                    }, 200)
                }


                UsbService.ACTION_USB_PERMISSION_NOT_GRANTED // USB PERMISSION NOT GRANTED
                -> {
                    handler.postDelayed({ display!!.append("ACTION_USB_PERMISSION_NOT_GRANTED\n") }, 200)
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show()
                }
                UsbService.ACTION_NO_USB // NO USB CONNECTED
                -> {
                    handler.postDelayed({ display!!.append("ACTION_NO_USB\n") }, 200)
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show()
                }
                UsbService.ACTION_USB_DISCONNECTED // USB DISCONNECTED
                -> {
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show()
                    handler.postDelayed({
                        connectedDataLogger = null
                        connectedProbeUUID = ""
                        display!!.text = ""
                        dataLoggerIdTextView!!.text = ""
                        lastDownloadDateTextView!!.text = ""
                    }, 200)
                }
                UsbService.ACTION_USB_NOT_SUPPORTED // USB NOT SUPPORTED
                -> {
                    handler.postDelayed({ display!!.append("ACTION_USB_NOT_SUPPORTED\n") }, 200)
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show()
                }
                UsbService.ACTION_USB_ATTACHED -> handler.postDelayed({ display!!.append("ACTION_USB_ATTACHED\n") }, 200)
            }//Toast.makeText(context, "USB Device Connected", Toast.LENGTH_SHORT).show();
        }
    }
    private var usbService: UsbService? = null
    private var display: TextView? = null
    private val editText: EditText? = null
    private var dataLoggerIdTextView: TextView? = null
    private var lastDownloadDateTextView: TextView? = null
    private var valueView1: TextView? = null
    private var valueView2: TextView? = null
    private var valueView3: TextView? = null


    private var mHandler: SerialDownloadFragment2.MyHandler? = null
    private val usbConnection = object : ServiceConnection {
        override fun onServiceConnected(arg0: ComponentName, arg1: IBinder) {
            usbService = (arg1 as UsbService.UsbBinder).service
            usbService!!.setHandler(mHandler)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            usbService = null
        }
    }

    private var control: Control? = null
    private var realm: Realm? = null
    private var connectedDataLogger: DataLogger? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mHandler = SerialDownloadFragment2.MyHandler(this)

        control = Control(context)
        control!!.setListener(this)

        Realm.init(context)
        realm = Realm.getDefaultInstance()
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.serial_download_fragment, container, false)

        display = view.findViewById(R.id.textView1) as TextView
        display!!.movementMethod = ScrollingMovementMethod()

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

        dataLoggerIdTextView = view.findViewById(R.id.dataloggerIdTextView) as TextView
        lastDownloadDateTextView = view.findViewById(R.id.lastDownloadDateTextView) as TextView

        valueView1 = view.findViewById(R.id.valueView1) as TextView
        valueView2 = view.findViewById(R.id.valueView2) as TextView
        valueView3 = view.findViewById(R.id.valueView3) as TextView

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

        val requestDownloadButton = view.findViewById(R.id.buttonRequestDownload) as Button
        requestDownloadButton.setOnClickListener {
            this@SerialDownloadFragment2.downloadRequestTime = System.currentTimeMillis() / 1000L

            if (connectedDataLogger != null) {
                val command = ">WT_DOWNLOAD:" + connectedDataLogger!!.lastDownloadedFileDate.toString() + "<"
                sendCommand(command)
            } else {
                display!!.append("No datalogger connected\n")
            }
        }

        val setRTCButton = view.findViewById(R.id.setRTC) as Button
        setRTCButton.setOnClickListener {
            val unixTime = System.currentTimeMillis() / 1000L
            if (connectedDataLogger != null) {
                val command = ">WT_SET_RTC:" + unixTime.toString() + "<"
                sendCommand(command)
            } else {
                display!!.append("No datalogger connected\n")
            }
        }

        val deployButton = view.findViewById(R.id.deployButton) as Button
        deployButton.setOnClickListener {
            val unixTime = System.currentTimeMillis() / 1000L
            if (connectedDataLogger != null) {
                val dv = unixTime * 1000// its need to be in milisecond
                val df = java.util.Date(dv)
                var deploymentIdentifier = SimpleDateFormat("yyyyMMddhhmmss").format(df)
                deploymentIdentifier = "SITENAME_$deploymentIdentifier"
                val command = ">WT_DEPLOY:$deploymentIdentifier<"
                sendCommand(command)
            } else {
                display!!.append("No datalogger connected\n")
            }
        }


        return view
    }

    fun send() {
        if (editText!!.text.toString() != "") {
            val data = editText.text.toString()
            if (usbService != null) { // if UsbService was correctly binded, Send data
                usbService!!.write(data.toByteArray())
            }
        }
    }

    fun sendCommand(command: String) {
        Log.d(TAG, "Send Command")
        display!!.append("Command:$command\n")
        if (usbService != null) { // if UsbService was correctly binded, Send data
            usbService!!.write(command.toByteArray())
        }
    }


    // Just for now...
    private val waterBearMACAddress = "D6:60:A6:55:2C:39"
    private val defaultDeviceMacAddress = waterBearMACAddress
    private var operationAttempt: Job? = null


    private val uartServiceUUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
    private val serialRXCharacteristicUUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")

    private suspend fun GattConnection.notifySerial() {
        val service = getService(uartServiceUUID)
                ?: throw IllegalStateException("service not found")
        val characteristic: BluetoothGattCharacteristic = service[serialRXCharacteristicUUID]
                ?: throw IllegalStateException("Characteristic not found")
        setCharacteristicNotificationsEnabled(characteristic, true);

        val CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"
        val clientCharacteristicConfiguration = UUID.fromString(CHARACTERISTIC_CONFIG)

        val config = characteristic.getDescriptor(clientCharacteristicConfiguration)
        if(config != null) {
            config.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            writeDescriptor(config)
        }

        Timber.i("OKO OK")
    }

    fun GattConnection.logConnectionChanges() {
        async(UI) {
            stateChangeChannel.consumeEach {
                Log.i("SerialDownloadFragment", "connection state changed: $it")
            }
        }
    }

    val connectionTimeoutInMillis = 5000

    override fun onResume() {
        super.onResume()
        setFilters()  // Start listening notifications from UsbService

        startService(UsbService::class.java, usbConnection, null) // Start UsbService(if it was not started before) and Bind it


        // Right here we will start working on the BLE connection
        var device = bluetoothManager.adapter.getRemoteDevice(defaultDeviceMacAddress)

        operationAttempt?.cancel()
        operationAttempt = launch(UI) {

            Timber.i("Connecting to BLE device")

            val deviceConnection = GattConnection(device)
            try {

                deviceConnection.logConnectionChanges()
                withTimeout(connectionTimeoutInMillis) {
                    deviceConnection.connect()
                }
                Timber.i("Connected!")
                val services = deviceConnection.discoverServices()
                Timber.i("Services discovered!")
                //block(deviceConnection, services)


                deviceConnection.notifySerial()
                Timber.i("Set up serial notifications!")


                async(UI) {

                    while(true){
                        val characteristicValue = deviceConnection.notifyChannel.receive()
                        val data = characteristicValue.getStringValue(0);
                        Timber.i("Notified Value: " + data);
                        // NOTE: if you are connected via both BLE and USB expect major strangeness
                        control?.receivedData(data) // TODO: Make this thread safe

                    }
/*                 deviceConnection.notifyChannel.consumeEach {
                        Timber.i("Notified Value: " + it.getStringValue(0));
                 }
                 */
                }



                Timber.i("Done somehow!")



            } catch (e: TimeoutCancellationException) {
                Timber.e("Connection timed out after $connectionTimeoutInMillis milliseconds!".also {
                    //toast(it)
                })
                throw e
            } catch (e: CancellationException) {
                Timber.e("v!")
                throw e
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                //deviceConnection.close()
                //Timber.i("Closed!")
            }

            /*device.useBasic(connectionTimeoutInMillis) { device, services ->
                services.forEach { Timber.d("Service found with UUID: ${it.uuid}") }
                with(GenericAccess) {
                    device.readAppearance()
                    Timber.d("Device appearance: ${device.appearance}")
                    device.readDeviceName()
                    Timber.d("Device name: ${device.deviceName}".also { toast(it) })
                    //device.readSerial()
                    //Timber.d("Device name: ${device.serialValue}".also { toast(it) })
                    device.notifySerial()
                }
                //device.notifyChannel.
            }*/
            operationAttempt = null
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(mUsbReceiver)
        activity?.unbindService(usbConnection)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm!!.close()
    }


    private fun startService(service: Class<*>, serviceConnection: ServiceConnection, extras: Bundle?) {
        if (!UsbService.SERVICE_CONNECTED) {
            val startService = Intent(activity, service)
            if (extras != null && !extras.isEmpty) {
                val keys = extras.keySet()
                for (key in keys) {
                    val extra = extras.getString(key)
                    startService.putExtra(key, extra)
                }
            }
            activity?.startService(startService)
        }
        val bindingIntent = Intent(activity, service)
        activity?.bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun setFilters() {
        val filter = IntentFilter()
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED)
        filter.addAction(UsbService.ACTION_USB_READY)
        filter.addAction(UsbService.ACTION_NO_USB)
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED)
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED)
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED)
        filter.addAction(UsbService.ACTION_USB_ATTACHED)
        activity?.registerReceiver(mUsbReceiver, filter)
    }

    private fun updateConnectedDatalogger(uuid: String) {
        connectedProbeUUID = uuid
        display!!.append("Looking for Datalogger object " + connectedProbeUUID!!)
        connectedDataLogger = realm!!.where(DataLogger::class.java).equalTo("UUID", connectedProbeUUID).findFirst()
        if (connectedDataLogger == null) {
            display!!.append("Created new Datalogger object")
            realm!!.beginTransaction()
            connectedDataLogger = realm!!.createObject(DataLogger::class.java)
            connectedDataLogger!!.uuid = uuid
            val selectedProject = Preferences.getSelectedProject(context, realm)
            selectedProject.dataLoggers.add(connectedDataLogger!!)
            realm!!.commitTransaction()
        }

        updateUI()
    }

    private fun updateUI() {
        dataLoggerIdTextView!!.text = "Current Datalogger UUID: " + connectedDataLogger!!.uuid
        lastDownloadDateTextView!!.text = "Last Download Date: " + connectedDataLogger!!.lastDownloadDate
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private class MyHandler(fragment: SerialDownloadFragment2) : Handler() {
        private val mFragment: WeakReference<SerialDownloadFragment2>

        init {
            mFragment = WeakReference(fragment)
        }

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                UsbService.MESSAGE_FROM_SERIAL_PORT -> {
                    val data = msg.obj as String
                    //mFragment.get().display.append("Data: " + data);
                    try {
                        Log.d(TAG, "Received Data $data")
                        if (mFragment.get() != null) {
                            mFragment.get()!!.control!!.receivedData(data)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        // We should probably reset the connction / switch back to control mode here.
                    }

                }
                UsbService.CTS_CHANGE -> Toast.makeText(mFragment.get()!!.activity, "CTS_CHANGE", Toast.LENGTH_LONG).show()
                UsbService.DSR_CHANGE -> Toast.makeText(mFragment.get()!!.activity, "DSR_CHANGE", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun processCommand(command: String) {
        display!!.append("Processing Command: $command -- \n\n")

        // If the command is AQ_TRANSFER_READY
        // then go into file transfer mode mode, writing all the data sent out to a text file
        if (command.contains("WT_IDENTIFY")) {
            val deviceUUID = command.substring(command.indexOf(":") + 1)
            display!!.append("GOT DEVICE UUID $deviceUUID\n\n")
            updateConnectedDatalogger(deviceUUID)


        } else if (command.contains("WT_READY")) {
            // we are in file transfer mode
            // switch to file transfer mode
            try {
                control!!.mode = Control.FILE_TRANSFER_MODE
            } catch (e: IOException) {
                e.printStackTrace()
            }

            // send ACK
            // NOTE: abstract usbServer and ble to one interface here
            // and only send to the one that is connected!
            usbService!!.write(Control.ACK.toByteArray())
            display!!.append("SEND " + Control.ACK + "\n\n")

        } else if (command.contains("WT_COMPLETE")) {
            if (command.contains(":")) {
                val lastDownloadFromDatalogger = command.substring(command.indexOf(":") + 1)
                // TODO store this lastDownloadFromDatalogger into the database on this device
                // which should also be updated from the server on syncs for this project.
                display!!.append("GOT LAST DOWNLOAD DATE $lastDownloadFromDatalogger\n\n")

                realm!!.beginTransaction()
                val dv = java.lang.Long.valueOf(this.downloadRequestTime) * 1000// its need to be in milisecond
                val df = java.util.Date(dv)
                val downloadDateString = SimpleDateFormat("MM dd, yyyy hh:mma").format(df)
                connectedDataLogger!!.lastDownloadedFileDate = lastDownloadFromDatalogger
                connectedDataLogger!!.lastDownloadDate = downloadDateString.toString()
                realm!!.commitTransaction()

                updateUI()
            }
        } else if (command.contains("WT_TIMESTAMP")) {
            if (command.contains(":")) {
                val timestampFromDatalogger = command.substring(command.indexOf(":") + 1)
                val timestamp = java.lang.Long.parseLong(timestampFromDatalogger)
                val dv = java.lang.Long.valueOf(timestamp) * 1000// its need to be in milisecond
                val df = java.util.Date(dv)
                val dateString = SimpleDateFormat("MM dd, yyyy hh:mma").format(df)
                display!!.append("CURRENT DATALOGGER TIME: $dateString")
            }
        } else if (command.contains("WT_VALUES")) {
            val valuesCSV = command.substring(command.indexOf(":") + 1)
            val valuesList = valuesCSV.split(",")
            valueView1?.text = valuesList[0]
            valueView2?.text = valuesList[2]
            valueView3?.text = valuesList[3]


        } else {
            display!!.append(command + "\n")
        }
    }

    override fun fileTransfered(fileTransferStorage: File) {
        // put file into the database so we can upload it later
        // this is where we need to at least now the device_id of this device

        val lastId = realm!!.where(DataLog::class.java).max("id")
        var nextID = 1
        if (lastId != null) {
            nextID = realm!!.where(DataLog::class.java).max("id").toInt() + 1 // TODO: not great
        }
        realm!!.beginTransaction()
        val dataLog = realm!!.createObject(DataLog::class.java, nextID)
        dataLog.isUploaded = false
        dataLog.probeUUID = connectedProbeUUID // TODO: Hard coded device Id
        dataLog.filePath = fileTransferStorage.path
        dataLog.dateRetreived = Date()
        realm!!.commitTransaction()
    }

    companion object {

        private val TAG = "SerialConsoleFragment"
    }


}
