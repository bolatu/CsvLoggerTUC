package com.bolatu.csvloggertuc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Created by bolatu on 19.05.17.
 */

public class BluetoothHandler {

    // Since we are running this WiFi scan process in the UI Thread,
    // we need pass the context from the activity
    private Context mContext;

    // Creating Bt variables
    private BluetoothScanReceiver btReceiver = new BluetoothScanReceiver();
    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    private AccessPointParser accessPointParser;

    // LinkedHashMap<SSID, LinkedList<HashMap<scanCounter, dB>>>
    private LinkedHashMap<String, LinkedList<HashMap<Integer, String>>> sortedBtScanResults = new LinkedHashMap<>();
    private LinkedList<String> timeStampHolderString = new LinkedList<>();
    private LinkedList<Long> timeStampHolderNanoseconds = new LinkedList<>();

    private HashMap<String, String> currentBtHashMap = new HashMap<>();

    private String rawBtScanResults = "";

    private TimeStampHandler timeStampHandler = new TimeStampHandler();
//    private CsvHandler csvHandler;

    private int scanCounter = 0;
    private int scanCounterTimeout = 0;

    private boolean isBtScanFinished = false;
    private boolean isScanSuccessfullyFinished = false;

    public ArrayAdapter<String> adapter;


    // WifiScanner's constructor
    public BluetoothHandler(Context context) {
        this.mContext = context;

        // Initiating wifi service manager
        btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        // Check for wifi is disabled
        if (!btAdapter.isEnabled()) {
            // If wifi disabled then enable it
            Toast.makeText(context.getApplicationContext(), "Bt is enabled!",
                    Toast.LENGTH_LONG).show();
            btAdapter.enable();
        }

        accessPointParser = new AccessPointParser(CsvHandler.BLUETOOTH);

    }

    // startMfScan registers the wifimanager and starts the scan
    public void startBtScan() {
        scanCounter = 0;
        scanCounterTimeout = 0;
        isBtScanFinished = false;
        isScanSuccessfullyFinished = false;

        // configuring the csvHandler as a wifi type
//        csvHandler = new CsvHandler(CsvHandler.BLUETOOTH);

        Log.d("BtScanReceiver", "BT scan is started");
        mContext.registerReceiver(btReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        mContext.registerReceiver(btReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        btAdapter.startDiscovery();

    }

    // stopMfScan unregisters the wifimanager
    public void stopBtScan() {
        if (!isBtScanFinished) {
            isBtScanFinished = true;
            Log.d("BtScanReceiver", "Bt scan is stopped");
            mContext.unregisterReceiver(btReceiver);
            accessPointParser.convertToCsv(sortedBtScanResults, scanCounter);
            sortedBtScanResults.clear();
            scanCounter = 0;
            scanCounterTimeout = 0;
        }
    }


    private class BluetoothScanReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismis progress dialog
                Log.d("BtDevices", "Discovery is finished");

                scanCounterTimeout++;
                Log.d("BtTimeout", String.valueOf(scanCounterTimeout));

                if (!currentBtHashMap.isEmpty()) {
                    sortedBtScanResults = accessPointParser.sortBtScanResults(currentBtHashMap, scanCounter);
                    currentBtHashMap.clear();
                    scanCounter++;
                    scanCounterTimeout = 0;

                    if (scanCounter < MainActivity.scanNo) {
                        btAdapter.startDiscovery();
                    } else {
                        isScanSuccessfullyFinished = true;
                        stopBtScan();
                    }
                }

//                Log.d("BtScanCounter", String.valueOf(scanCounter));

                if (scanCounterTimeout < 5 && scanCounterTimeout > 0) {
                    btAdapter.startDiscovery();
                }
                else if (scanCounterTimeout == 5){
                    isScanSuccessfullyFinished = false;
                    stopBtScan();
                }

            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);

                if ((device.getName() != null) && (device.getName().length() > 0)) {

                    Log.d("BtDevices", "A device found");
                    currentBtHashMap.put(device.getAddress() + "(" + device.getName() + ")",
                                String.valueOf(rssi));

                }
            }

        }
    };



    public boolean isBtScanFinished() {
        return isBtScanFinished;
    }

    public int getScanCounterTimeout() {
        return scanCounterTimeout;
    }

    public int getScanCounter() {
        return scanCounter;
    }

    public boolean isScanSuccessfullyFinished() {
        return isScanSuccessfullyFinished;
    }

    public void setBtScanFinished(boolean wifiScanFinished) {
        isBtScanFinished = wifiScanFinished;
    }

    public void setScanSuccessfullyFinished(boolean ScanSuccessfullyFinished) {
        isScanSuccessfullyFinished = ScanSuccessfullyFinished;
    }
}
