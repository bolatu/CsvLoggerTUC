/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.bolatu.csvloggertuc.CellScanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

//import org.mozilla.mozstumbler.service.AppGlobals;
//import org.mozilla.mozstumbler.service.Prefs;
//import org.mozilla.mozstumbler.service.stumblerthread.Reporter;

import com.bolatu.csvloggertuc.AccessPointParser;
import com.bolatu.csvloggertuc.CsvHandler;
import com.bolatu.csvloggertuc.MainActivity;
import com.bolatu.csvloggertuc.TimeStampHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CellScanner implements IHaltable {

    private TimeStampHandler timeStampHandler = new TimeStampHandler();

    private long timestampOfDetection;
    private String convertedTimestamp;

    private boolean isScanFinished = false;
    private boolean isScanSuccessfullyFinished = false;

    AccessPointParser accessPointParser;

    private LinkedHashMap<String, LinkedList<HashMap<Integer, String>>> sortedWifiScanResults = new LinkedHashMap<>();
    private HashMap<String, String> currentWifiScanResultList = new HashMap<>();

    private String rawWifiScanResults = "";

    private CsvHandler csvHandler;

    private int scanCounter = 0;
    private int scanCounterTimeout = 0;

//    public static final String ACTION_BASE = AppGlobals.ACTION_NAMESPACE + ".CellScanner.";
//    public static final String ACTION_CELLS_SCANNED = ACTION_BASE + "CELLS_SCANNED";
    public static final String ACTION_CELLS_SCANNED_ARG_CELLS = "cells";
//    public static final String ACTION_CELLS_SCANNED_ARG_TIME = AppGlobals.ACTION_ARG_TIME;

    private static final int MAX_SCANS_PER_GPS = 2;
    private static final long CELL_MIN_UPDATE_TIME = 2000; // milliseconds

    private final Context mAppContext;
    private final Set<String> mVisibleCells = new HashSet<String>();
    private final ReportFlushedReceiver mReportFlushedReceiver = new ReportFlushedReceiver();
    private final AtomicBoolean mReportWasFlushed = new AtomicBoolean();
    private final ISimpleCellScanner mSimpleCellScanner;
    private Timer mCellScanTimer;
//    private Handler mBroadcastScannedHandler;
    private AtomicInteger mScanCount = new AtomicInteger();

    private final TreeMap<String, CellInfo> mCellData = new TreeMap<String, CellInfo>();

    public CellScanner(Context appCtx) {
        mAppContext = appCtx;
        mSimpleCellScanner = new SimpleCellScannerImplementation(mAppContext);

        accessPointParser = new AccessPointParser(CsvHandler.CELL);
    }

    public void start() {
        if (!mSimpleCellScanner.isSupportedOnThisDevice()) {
            return;
        }

        // If the scan timer is active, this will reset the number of times it has run
        mScanCount.set(0);

        // clear cells on next scan
        mReportWasFlushed.set(true);

        if (mCellScanTimer != null) {
            return;
        }

        isScanSuccessfullyFinished = false;
        isScanFinished = false;


            mSimpleCellScanner.start();

            mCellScanTimer = new Timer();

            mCellScanTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    if (!mSimpleCellScanner.isStarted()) {
                        return;
                    }

//                    if (mScanCount.incrementAndGet() > MAX_SCANS_PER_GPS) {
//                        stop();
//                        return;
//                    }

                    final long curTime = System.currentTimeMillis();
                    ArrayList<CellInfo> cells = new ArrayList<CellInfo>(mSimpleCellScanner.getCellInfo());
                    Log.d("CellularScan:", cells.toString());

                    if (mReportWasFlushed.getAndSet(false)) {
                        clearCells();
                    }

                    if (cells.isEmpty()) {
                        scanCounterTimeout++;
                        return;
                    }

                    for (CellInfo cell : cells) {
                        addToCells(cell.getCellIdentity());
                        currentWifiScanResultList.put(cell.getCellIdentity(),
                                cell.getCellRadio());
                        Log.d("CellularScan:", cell.getCellIdentity());
                        currentWifiScanResultList.put(cell.getCellIdentity(),
                                String.valueOf(cell.getSignalStrength()));
                    }

                    String convertedMaxTimestamp = timeStampHandler.convertTimestamp(timestampOfDetection);

                    sortedWifiScanResults = accessPointParser.sortBtScanResults(currentWifiScanResultList, scanCounter);

                    scanCounter++;

                    if (scanCounter >= MainActivity.scanNo) {
                        stop();
                    }
                    else if (scanCounterTimeout == 15){
                        isScanSuccessfullyFinished = false;
                        stop();
                    }


//                Intent intent = new Intent(ACTION_CELLS_SCANNED);
//                intent.putParcelableArrayListExtra(ACTION_CELLS_SCANNED_ARG_CELLS, cells);
//                intent.putExtra(ACTION_CELLS_SCANNED_ARG_TIME, curTime);
//                // send to handler, so broadcast is not from timer thread
//                Message message = new Message();
//                message.obj = intent;
//                mBroadcastScannedHandler.sendMessage(message);
                }
            }, 0, CELL_MIN_UPDATE_TIME);
        }


    private synchronized void clearCells() {
        mVisibleCells.clear();
    }

    private synchronized void addToCells(String cell) {
        mVisibleCells.add(cell);
    }

    public void addCellData(String key, CellInfo result) {
        if (!mCellData.containsKey(key)) {
            mCellData.put(key, result);
        }
    }

    public synchronized void stop() {
        LocalBroadcastManager.getInstance(mAppContext).unregisterReceiver(mReportFlushedReceiver);

        isScanFinished = true;
        if (mCellScanTimer != null) {
            mCellScanTimer.cancel();
            mCellScanTimer = null;
        }
        mSimpleCellScanner.stop();

        accessPointParser.convertToCsv(sortedWifiScanResults, scanCounter);
        sortedWifiScanResults.clear();
        isScanSuccessfullyFinished = true;
        scanCounter = 0;
    }

    public synchronized int getVisibleCellInfoCount() {
        return mVisibleCells.size();
    }

    private class ReportFlushedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent i) {
            mReportWasFlushed.set(true);
        }
    }

    public boolean isScanSuccessfullyFinished() {
        return isScanSuccessfullyFinished;
    }

    public boolean isScanFinished() {
        return isScanFinished;
    }

    public void setScanFinished(boolean scanFinished) {
        isScanFinished = scanFinished;
    }

    public void setScanSuccessfullyFinished(boolean ScanSuccessfullyFinished) {
        isScanSuccessfullyFinished = ScanSuccessfullyFinished;
    }
}
