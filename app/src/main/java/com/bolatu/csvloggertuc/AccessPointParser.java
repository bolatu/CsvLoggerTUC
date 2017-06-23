package com.bolatu.csvloggertuc;

import android.net.wifi.ScanResult;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by bolatu on 19.05.17.
 */

public class AccessPointParser {

    private LinkedHashMap<String, LinkedList<HashMap<Integer, String>>> sortedScanResults = new LinkedHashMap<>();
    private LinkedList<String> timeStampHolderString = new LinkedList<>();
    private LinkedList<Long> timeStampHolderNanoseconds = new LinkedList<>();

    private CsvHandler csvHandler;
    private TimeStampHandler timeStampHandler = new TimeStampHandler();

    public AccessPointParser(int csvHandlerString) {
        switch (csvHandlerString){
            case CsvHandler.WIFI:
                csvHandler = new CsvHandler(CsvHandler.WIFI);
                return;
            case CsvHandler.BLUETOOTH:
                csvHandler = new CsvHandler(CsvHandler.BLUETOOTH);
                return;
            case CsvHandler.CELL:
                csvHandler = new CsvHandler(CsvHandler.CELL);
                return;
        }
    }

    public String convertToCsv(LinkedHashMap<String, LinkedList<HashMap<Integer, String>>> sortedScanResults, int scanCounter) {

        LinkedHashMap<String, LinkedList<HashMap<Integer, String>>> tempSortedScanResults;
        tempSortedScanResults = sortedScanResults;

        // Preparing the Wifi Header
        String csvHeader = "Coor X,Coor Y,CURRENT TIME NANOSECONDS,CURRENT TIME STRING,";
        for (String sortedHashMapKey : sortedScanResults.keySet()) {
            csvHeader += sortedHashMapKey + ",";
        }
        csvHeader = csvHeader.substring(0, csvHeader.length() - 1);
        csvHeader += "\n";
        Log.d("WifiHeader", csvHeader);


        // Filling the missing Wifi Scan Results with the NaN
        for (String sortedHashMapKey : sortedScanResults.keySet()) {
            LinkedList<HashMap<Integer, String>> tempLinkedList = sortedScanResults.get(sortedHashMapKey);

            // Before start filling, which scanCounter's Wifi Results are missing need to be detected
            if (tempLinkedList.size() < scanCounter) {
                ArrayList<Integer> order = new ArrayList<>();
                for (int i = 0; i < tempLinkedList.size(); i++) {
                    HashMap<Integer, String> tempHashMap = tempLinkedList.get(i);
                    for (Integer orderKey : tempHashMap.keySet()) {
                        order.add(orderKey);
                    }
                }

                // Then, start filling the missing scan results w/ NaNs
                for (int k = 0; k < scanCounter; k++) {
                    if (!order.contains(k)) {
                        LinkedHashMap<Integer, String> tempHashMap = new LinkedHashMap<>();
                        tempHashMap.put(k, "NaN");
                        tempLinkedList.add(k, tempHashMap);
//                        Log.d("FillingWithNaNs1", String.valueOf(k));
                    }
                }
            }
            tempSortedScanResults.put(sortedHashMapKey, tempLinkedList);
        }

        // storing back the Wifi Scan Results filled with NaNs
        sortedScanResults = tempSortedScanResults;

        // converting sortedScanResults into csv format
        String rows = "";
        for (int i = 0; i < scanCounter; i++) {
            rows += MainActivity.coorX + "," + MainActivity.coorY + "," + timeStampHolderNanoseconds.get(i) + "," + timeStampHolderString.get(i) + ",";
            for (String ssid : sortedScanResults.keySet()) {
                rows += sortedScanResults.get(ssid).get(i).get(i) + ",";
            }
            rows = rows.substring(0, rows.length() - 1);
            rows += "\n";
        }

        Log.d("rowContent", rows);

        try {
//            csvHandler.writeHeader(wifiHeader);
            csvHandler.write(csvHeader);
            csvHandler.write(rows);
            timeStampHolderString.clear();
            timeStampHolderNanoseconds.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }

    public LinkedHashMap<String, LinkedList<HashMap<Integer, String>>> sortWifiScanResults(List<ScanResult> wifiScanResultList, int scanCounter) {

        LinkedHashMap<String, LinkedList<HashMap<Integer, String>>> tempSortedWifiScanResults;

        // Converting the format into HashMap style, which is key = SSID, value = dB
        HashMap<String, String> currentWifiHashMap = new HashMap<>();
        for (int i = 0; i < wifiScanResultList.size(); i++) {
            currentWifiHashMap.put(wifiScanResultList.get(i).BSSID + "(" + wifiScanResultList.get(i).SSID + ")",
                    String.valueOf(wifiScanResultList.get(i).level));
        }

        // if this is the first time of the scan, it needs to store the results into sortedScanResults.
        if (scanCounter == 0) {
            for (String currentWifiHashMapKey : currentWifiHashMap.keySet()) {
                LinkedList tempSortedLinkedList = new LinkedList();
                HashMap<Integer, String> tempHashMap = new HashMap<>();
                tempHashMap.put(0, currentWifiHashMap.get(currentWifiHashMapKey));
                tempSortedLinkedList.add(tempHashMap);
                sortedScanResults.put(currentWifiHashMapKey, tempSortedLinkedList);
            }
        } else {
            // saving the older records into temp before iterating the sortedScanResults
            tempSortedWifiScanResults = sortedScanResults;

//                    Log.d("WifiScanCounter", String.valueOf(scanCounter));

            for (String currentWifiHashMapKey : currentWifiHashMap.keySet()) {

//                        Log.d("currentWifiHashMap", "iterating");

                for (String sortedWifiScanResultsKey : sortedScanResults.keySet()) {

                    if (sortedWifiScanResultsKey.equals(currentWifiHashMapKey)) {

                        LinkedList tempLinkedList = sortedScanResults.get(sortedWifiScanResultsKey);

                        HashMap<Integer, String> tempSortedHashMap = new HashMap<>();

                        tempSortedHashMap.put(scanCounter, currentWifiHashMap.get(currentWifiHashMapKey));
                        tempLinkedList.add(tempSortedHashMap);

                        // updating the tempSortedLinkedList to able to iterate the sortedScanResults
                        tempSortedWifiScanResults.put(sortedWifiScanResultsKey, tempLinkedList);
//                                Log.d("tempSortedWifiScanResults", "is updated");
                    }

                }

            }

            // updating the sortedScanResults
            sortedScanResults = tempSortedWifiScanResults;

            for (String sortedWifiHashMapKey : sortedScanResults.keySet()) {
                Log.d("Updated Wifi", sortedWifiHashMapKey + ": " + sortedScanResults.get(sortedWifiHashMapKey));
            }

        }

        timeStampHolderString.add(timeStampHandler.updateTimeString());
        timeStampHolderNanoseconds.add(timeStampHandler.updateTimeNanoseconds());

        return sortedScanResults;
    }


    public LinkedHashMap<String, LinkedList<HashMap<Integer, String>>> sortBtScanResults(HashMap<String, String>  btScanResultList, int scanCounter) {

        LinkedHashMap<String, LinkedList<HashMap<Integer, String>>> tempSortedBtScanResults;

        // Converting the format into HashMap style, which is key = SSID, value = dB
        HashMap<String, String> currentBtHashMap = btScanResultList;

        // if this is the first time of the scan, it needs to store the results into sortedScanResults.
        if (scanCounter == 0) {
            for (String currentBtHashMapKey : currentBtHashMap.keySet()) {
                LinkedList tempSortedLinkedList = new LinkedList();
                HashMap<Integer, String> tempHashMap = new HashMap<>();
                tempHashMap.put(0, currentBtHashMap.get(currentBtHashMapKey));
                tempSortedLinkedList.add(tempHashMap);
                sortedScanResults.put(currentBtHashMapKey, tempSortedLinkedList);
            }
        } else {
            // saving the older records into temp before iterating the sortedScanResults
            tempSortedBtScanResults = sortedScanResults;

//                    Log.d("WifiScanCounter", String.valueOf(scanCounter));

            for (String currentBtHashMapKey : currentBtHashMap.keySet()) {

//                        Log.d("currentWifiHashMap", "iterating");

                for (String sortedBtScanResultsKey : sortedScanResults.keySet()) {

                    if (sortedBtScanResultsKey.equals(currentBtHashMapKey)) {

                        LinkedList tempLinkedList = sortedScanResults.get(sortedBtScanResultsKey);

                        HashMap<Integer, String> tempSortedHashMap = new HashMap<>();

                        tempSortedHashMap.put(scanCounter, currentBtHashMap.get(currentBtHashMapKey));
                        tempLinkedList.add(tempSortedHashMap);

                        // updating the tempSortedLinkedList to able to iterate the sortedScanResults
                        tempSortedBtScanResults.put(sortedBtScanResultsKey, tempLinkedList);
//                                Log.d("tempSortedWifiScanResults", "is updated");
                    }

                }

            }

            // updating the sortedScanResults
            sortedScanResults = tempSortedBtScanResults;

            for (String sortedWifiHashMapKey : sortedScanResults.keySet()) {
                Log.d("Updated BT", sortedWifiHashMapKey + ": " + sortedScanResults.get(sortedWifiHashMapKey));
            }

        }

        timeStampHolderString.add(timeStampHandler.updateTimeString());
        timeStampHolderNanoseconds.add(timeStampHandler.updateTimeNanoseconds());

        return sortedScanResults;
    }
}
