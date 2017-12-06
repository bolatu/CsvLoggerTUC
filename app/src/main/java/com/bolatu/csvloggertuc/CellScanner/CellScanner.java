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

    private boolean isScanFinished = false;
    private boolean isScanSuccessfullyFinished = false;

    AccessPointParser accessPointParser;

    private LinkedHashMap<String, LinkedList<HashMap<Integer, String>>> sortedCellScanResults = new LinkedHashMap<>();
    private HashMap<String, String> currentCellScanResultList = new HashMap<>();

    private int scanCounter = 0;
    private int scanCounterTimeout = 0;

    private static final long CELL_MIN_UPDATE_TIME = 200; // milliseconds

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
            isScanFinished = true;
            isScanSuccessfullyFinished = false;
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


                    final long curTime = System.currentTimeMillis();
                    ArrayList<CellInfo> cells = new ArrayList<CellInfo>(mSimpleCellScanner.getCellInfo());

                    if (mReportWasFlushed.getAndSet(false)) {
                        clearCells();
                    }

                    if (cells.isEmpty()) {
                        scanCounterTimeout++;
                    }

                    for (CellInfo cell : cells) {
                        addToCells(cell.getCellIdentity());
                        Log.d("CellularScan:", cell.getCellIdentity() + String.valueOf(cell.getSignalStrength()));
                        currentCellScanResultList.put(cell.getCellIdentity(),
                                String.valueOf(cell.getSignalStrength()));
                    }


                    sortedCellScanResults = accessPointParser.sortBtScanResults(currentCellScanResultList, scanCounter);

                    scanCounter++;

                    if (scanCounter > MainActivity.scanNo) {
                        stop();
                    }
                    else if (scanCounterTimeout > 20){
                        isScanSuccessfullyFinished = false;
                        stop();
                    }

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

        accessPointParser.convertToCsv(sortedCellScanResults, scanCounter);
        sortedCellScanResults.clear();
        isScanSuccessfullyFinished = true;
        scanCounter = 0;
        scanCounterTimeout = 0;
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
    public int getScanCounterTimeout() {
        return scanCounterTimeout;
    }

    public int getScanCounter() {
        return scanCounter;
    }
}
