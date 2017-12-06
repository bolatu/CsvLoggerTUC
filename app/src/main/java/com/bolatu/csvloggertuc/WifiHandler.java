package com.bolatu.csvloggertuc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by bolatu on 5/13/16.
 */
public class WifiHandler {
    // Since we are running this WiFi scan process in the UI Thread,
    // we need pass the context from the activity
    private Context mContext;

    // Creating WiFi variables
    private WifiScanReceiver wifiReceiver = new WifiScanReceiver();
    private WifiManager wifiManager;

    AccessPointParser accessPointParser = new AccessPointParser(CsvHandler.WIFI);

    private LinkedHashMap<String, LinkedList<HashMap<Integer, String>>> sortedWifiScanResults = new LinkedHashMap<>();

    private String rawWifiScanResults = "";

//    private CsvHandler csvHandler;

    private int scanCounter = 0;
    private int scanCounterTimeout = 0;

    private boolean isWifiScanFinished = false;
    private boolean isScanSuccessfullyFinished = false;

    // WifiScanner's constructor
    public WifiHandler(Context context) {
        this.mContext = context;

        // Initiating wifi service manager
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        // Check for wifi is disabled
        if (!wifiManager.isWifiEnabled()) {
            // If wifi disabled then enable it
            Toast.makeText(context.getApplicationContext(), "Wifi is enabled!",
                    Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

    }

    // startMfScan registers the wifimanager and starts the scan
    public void startWifiScan() {
        scanCounter = 0;
        scanCounterTimeout = 0;
        isWifiScanFinished = false;
        isScanSuccessfullyFinished = false;

        // configuring the csvHandler as a wifi type
//        csvHandler = new CsvHandler(CsvHandler.WIFI);

        Log.d("WifiScanReceiver", "Wifi scan is started");
        mContext.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

    // stopMfScan unregisters the wifimanager
    public void stopWifiScan() {
        if (!isWifiScanFinished) {
            isWifiScanFinished = true;
            Log.d("WifiScanReceiver", "Wifi scan is stopped");
            mContext.unregisterReceiver(wifiReceiver);
            accessPointParser.convertToCsv(sortedWifiScanResults, scanCounter);
            sortedWifiScanResults.clear();
            scanCounter = 0;
            scanCounterTimeout = 0;
        }
    }

    private class WifiScanReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {

            // When wifi scan results are available to read, it goes inside
            String action = intent.getAction();
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {

                if (WifiManager.EXTRA_NEW_RSSI.equals(action)) {
                    Log.d("WifiScanReceiver", "newRSSI");
                }

                    Log.d("WifiScanReceiver", "Wifi Scan is received");

                // Getting wifi scan results
                List<ScanResult> currentWifiScanResultList = wifiManager.getScanResults();

                scanCounterTimeout++;

                if (!currentWifiScanResultList.isEmpty()) {
                    sortedWifiScanResults = accessPointParser.sortWifiScanResults(currentWifiScanResultList, scanCounter);
                    scanCounter++;
                    scanCounterTimeout = 0;

                    // Remember to change this max scan number!!!!!!!!!!!
                    if (scanCounter < MainActivity.scanNo) {
                        wifiManager.startScan();
                    } else {
                        isScanSuccessfullyFinished = true;
                        stopWifiScan();
                    }
                }

                if (scanCounterTimeout < 5 && scanCounterTimeout > 0) {
                    wifiManager.startScan();
                }
                else if (scanCounterTimeout == 5){
                    isScanSuccessfullyFinished = false;
                    stopWifiScan();
                }

            }
        }
    }

    public String getRawWifiScanResults() {
        return rawWifiScanResults;
    }

    public boolean isWifiScanFinished() {
        return isWifiScanFinished;
    }

    public void setWifiScanFinished(boolean wifiScanFinished) {
        isWifiScanFinished = wifiScanFinished;
    }

    public void setScanSuccessfullyFinished(boolean ScanSuccessfullyFinished) {
        isScanSuccessfullyFinished = ScanSuccessfullyFinished;
    }

    public boolean isScanSuccessfullyFinished() {
        return isScanSuccessfullyFinished;
    }

    public int getScanCounter() {
        return scanCounter;
    }

    public int getScanCounterTimeout() {
        return scanCounterTimeout;
    }

}
