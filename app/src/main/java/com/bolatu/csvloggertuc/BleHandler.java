package com.bolatu.csvloggertuc;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Created by bolatu on 06/10/17.
 */

public class BleHandler extends ListActivity{

    private Context mContext;

    private AccessPointParser accessPointParser;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothScanner;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;


    // LinkedHashMap<SSID, LinkedList<HashMap<scanCounter, dB>>>
    private LinkedHashMap<String, LinkedList<HashMap<Integer, String>>> sortedBtScanResults = new LinkedHashMap<>();
    private LinkedList<String> timeStampHolderString = new LinkedList<>();
    private LinkedList<Long> timeStampHolderNanoseconds = new LinkedList<>();

    private HashMap<String, String> currentBtHashMap = new HashMap<>();

    private int scanCounter = 0;
    private int scanCounterTimeout = 0;

    private boolean isBtScanFinished = false;
    private boolean isScanSuccessfullyFinished = false;


    public BleHandler(Context context) {
        this.mContext = context;

        mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
        // Initiating wifi service manager
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        // Check for wifi is disabled
        if (!mBluetoothAdapter.isEnabled()) {
            // If wifi disabled then enable it
            Toast.makeText(context.getApplicationContext(), "Bt is enabled!",
                    Toast.LENGTH_LONG).show();
            mBluetoothAdapter.enable();
        }

        accessPointParser = new AccessPointParser(CsvHandler.BLUETOOTH);
    }



//    // Device scan callback.
//    public BluetoothAdapter.LeScanCallback mLeScanCallback =
//            new BluetoothAdapter.LeScanCallback() {
//                @Override
//                public void onLeScan(final BluetoothDevice device, final int rssi,
//                                     byte[] scanRecord) {
//
//                    Log.d("BtScanReceiver", "Device Name: " + device.getName() + " rssi: " + rssi + "\n");
//                    currentBtHashMap.put(device.getAddress() + "(" + device.getName() + ")",
//                            String.valueOf(rssi));
//
//                    if (!currentBtHashMap.isEmpty()) {
//                        sortedBtScanResults = accessPointParser.sortBtScanResults(currentBtHashMap, scanCounter);
//                        currentBtHashMap.clear();
//                        scanCounter++;
//                        scanCounterTimeout = 0;
//
//                        if (scanCounter > MainActivity.scanNo) {
//                            stopBtScan();
//                        }
//
//                    }
//
//                }
//            };



    // Scan Interval Handler to read the result from BLE
    Handler handler = new Handler();
    Runnable runnableCode = new Runnable() {
        @Override
        public void run() {

            if (isBtScanFinished)
                return;

            scanCounterTimeout++;

            if (!btScanResultList.isEmpty()) {

                sortedBtScanResults = accessPointParser.sortBtScanResults(btScanResultList, scanCounter);
                btScanResultList.clear();
                scanCounter++;
                scanCounterTimeout = 0;

//                if (scanCounter > MainActivity.scanNo * 5) {
//                    isScanSuccessfullyFinished = true;
//                    stopBtScan();
//                }

            }

            if (scanCounterTimeout > 1000){
                isScanSuccessfullyFinished = false;
                stopBtScan();
            }



            handler.postDelayed(this, 5);
        }
    };


    // Handler to end the scan
    Handler handlerEnd = new Handler();
    // Define the code block to be executed
    private Runnable runnableCodeEnd = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            Log.d("Handlers", "Called on main thread");
            isScanSuccessfullyFinished = true;
            stopBtScan();
        }
    };


    public void startBtScan() {
        scanCounter = 0;
        scanCounterTimeout = 0;
        isBtScanFinished = false;
        isScanSuccessfullyFinished = false;
        System.out.println("start scanning");

        // init the reading handler
        handler.post(runnableCode);
        // init the ender handler (scanNo * 1 second)
        handlerEnd.postDelayed(runnableCodeEnd, MainActivity.scanNo*1000);

        mBluetoothAdapter.startLeScan(mLeScanCallback);

    }

    public void stopBtScan() {
        if (!isBtScanFinished) {
            isBtScanFinished = true;
            Log.d("BtScanReceiver", "Bt scan is stopped");
            //mContext.unregisterReceiver(btReceiver);
            accessPointParser.convertToCsv(sortedBtScanResults, scanCounter);
            sortedBtScanResults.clear();
            scanCounter = 0;
            scanCounterTimeout = 0;

            try {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } catch (NullPointerException exception) {
                Log.e("BtScanReceiver", "Can't stop scan. Unexpected NullPointerException", exception);
            }

        }
    }


    HashMap<String, String>  btScanResultList = new HashMap<>();

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("BtScanReceiver", "Device Name: " + device.getName() + " rssi: " + rssi + "\n");

                            btScanResultList.put(device.getAddress() + "(" + device.getName() + ")",
                                    String.valueOf(rssi));


                        }
                    });
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

