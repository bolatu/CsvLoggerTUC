package com.bolatu.csvloggertuc;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.bolatu.csvloggertuc.CellScanner.CellScanner;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private boolean isTestRunning = false;

    public static Button buttonStart;
    private EditText editTextX;
    private EditText editTextY;
    private EditText editTextScanNo;
    private CheckBox checkBoxWifi;
    private CheckBox checkBoxMf;
    private CheckBox checkBoxBt;
    private CheckBox checkBoxCell;

    private NotificationSound notificationSound;

    public static int coorX = 0;
    public static int coorY = 0;

    public static int scanNo = 1;

    // ListView
    private ListView listView;
    public static ArrayList<String> listViewHolder = new ArrayList<String>();
    public ArrayAdapter<String> adapter;
    private Handler listViewHandler = new Handler();


    private MagneticFieldHandler magneticFieldHandler;
    private WifiHandler wifiHandler;
    private BluetoothHandler bluetoothHandler;
    private CellScanner cellScanner;

    // Creating Runnable to update the ListView periodically
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            listViewHolder.clear();
            String[] listViewContent = getListViewContent();
            for (int i = 0; i < listViewContent.length; ++i) {
                listViewHolder.add(listViewContent[i]);
            }

            if (magneticFieldHandler.isScanFinished() && wifiHandler.isWifiScanFinished() && bluetoothHandler.isBtScanFinished() &&
                   cellScanner.isScanFinished() && isTestRunning){
                buttonStart.setText("START");
                isTestRunning = false;
                listViewHandler.removeCallbacks(runnableCode);

//                if (magneticFieldHandler.isScanSuccessfullyFinished() && wifiHandler.isScanSuccessfullyFinished() && bluetoothHandler.isScanSuccessfullyFinished()) {
                    notificationSound.playSuccessSound();
//                }
//                else{
//                    notificationSound.playErrorSound();
//                }

                // Get the intent that started this activity
                Intent intent = getIntent();
                String action = intent.getAction();
                String type = intent.getType();
                if ( action.equals("com.example.MozillaTUCMap") && type != null) {
                    Intent result = new Intent("com.example.MozillaTUCMap");
                    result.putExtra("MF", String.valueOf(magneticFieldHandler.isScanSuccessfullyFinished()));
                    result.putExtra("WIFI", String.valueOf(wifiHandler.isScanSuccessfullyFinished()));
                    result.putExtra("BT", String.valueOf(bluetoothHandler.isScanSuccessfullyFinished()));
                    result.putExtra("CELL", String.valueOf(cellScanner.isScanSuccessfullyFinished()));
                    setResult(Activity.RESULT_OK, result);
                    finish();
                }
            }

            adapter.notifyDataSetChanged();
            // Repeat this the same runnable code block again
            listViewHandler.postDelayed(runnableCode, 200);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // If the Android version is 6.0 or higher, we have to take these stupid permission on runtime
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0x12345);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
            boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(!statusOfGPS){
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }

        buttonStart = (Button) findViewById(R.id.buttonStart);
        listView = (ListView) findViewById(R.id.listView);
        editTextX = (EditText) findViewById(R.id.editTextX);
        editTextY = (EditText) findViewById(R.id.editTextY);
        editTextScanNo = (EditText) findViewById(R.id.editTextScanNo);
        checkBoxWifi = (CheckBox) findViewById(R.id.checkBoxWifi);
        checkBoxMf = (CheckBox) findViewById(R.id.checkBoxMf);
        checkBoxBt = (CheckBox) findViewById(R.id.checkBoxBt);
        checkBoxCell = (CheckBox) findViewById(R.id.checkBoxCell);

        notificationSound = new NotificationSound(getApplicationContext());

        magneticFieldHandler = new MagneticFieldHandler(getApplicationContext());
        wifiHandler = new WifiHandler(getApplicationContext());
        bluetoothHandler = new BluetoothHandler(getApplicationContext());
        cellScanner = new CellScanner(getApplicationContext());

        adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, android.R.id.text1, listViewHolder);
        listView.setAdapter(adapter);

        // Get the intent that started this activity
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if ( action.equals("com.example.MozillaTUCMap") && type != null) {
            if ("text/plain".equals(type)) {
                Log.d("DummyMapApp", "started this app!");
                Log.d("DummyMapApp", intent.getExtras().getString("X"));
                editTextX.setText(intent.getExtras().getString("X"));
                Log.d("DummyMapApp", intent.getExtras().getString("Y"));
                editTextY.setText(intent.getExtras().getString("Y"));
                Log.d("DummyMapApp", intent.getExtras().getString("FLOOR"));
            }
        }

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isTestRunning){
                    buttonStart.setText("START");
                    notificationSound.playErrorSound();
                    if (checkBoxCell.isChecked()){
                        cellScanner.stop();
                    }
                    if (checkBoxMf.isChecked()){
                        magneticFieldHandler.stopMfScan();
                    }
                    if (checkBoxWifi.isChecked()){
                        wifiHandler.stopWifiScan();
                    }
                    if (checkBoxBt.isChecked()){
                        bluetoothHandler.stopBtScan();
                    }
                    if (checkBoxCell.isChecked()){
                        cellScanner.stop();
                    }
                    isTestRunning = false;
                    listViewHandler.removeCallbacks(runnableCode);
                }
                else {
                    if(editTextX.getText().toString().equals("") || editTextY.getText().toString().equals("") || editTextScanNo.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "Hop! Enter Coordinates!", Toast.LENGTH_SHORT).show();
                    }
                    else if(!checkBoxWifi.isChecked() && !checkBoxMf.isChecked() && !checkBoxBt.isChecked() && !checkBoxCell.isChecked()){
                        Toast.makeText(getApplicationContext(), "Hop! Choose Test Type!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        buttonStart.setText("SCANNING!..");
//                        buttonStart.setEnabled(false);

                        coorX = Integer.parseInt(editTextX.getText().toString());
                        coorY = Integer.parseInt(editTextY.getText().toString());
                        scanNo = Integer.parseInt(editTextScanNo.getText().toString());

                        if (checkBoxCell.isChecked()) {
                            cellScanner.start();
                        }
                        else{
                            cellScanner.setScanFinished(true);
                            cellScanner.setScanSuccessfullyFinished(false);
                        }

                        if (checkBoxMf.isChecked()) {
                            magneticFieldHandler.startMfScan();
                        }
                        else{
                            magneticFieldHandler.setScanFinished(true);
                            magneticFieldHandler.setScanSuccessfullyFinished(false);
                        }

                        if (checkBoxWifi.isChecked()){
                            wifiHandler.startWifiScan();
                        }
                        else{
                            wifiHandler.setWifiScanFinished(true);
                            wifiHandler.setScanSuccessfullyFinished(false);
                        }

                        if (checkBoxBt.isChecked()){
                            bluetoothHandler.startBtScan();
                        }
                        else{
                            bluetoothHandler.setBtScanFinished(true);
                            bluetoothHandler.setScanSuccessfullyFinished(false);
                        }

                        isTestRunning = true;
                        listViewHandler.post(runnableCode);
                    }
                }
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0x12345) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
        }
    }


    public String[] getListViewContent(){
        String[] values = new String[]{
                "ScanCounter MF/WIFI/BT " + String.valueOf(magneticFieldHandler)+ "\n",
                "LOCAL MF:" + "\n" +
                        "X: " + String.valueOf(magneticFieldHandler.getLocalMfX()) + "\n" +
                        "Y: " + String.valueOf(magneticFieldHandler.getLocalMfY()) + "\n" +
                        "Z: " + String.valueOf(magneticFieldHandler.getLocalMfZ()),
                "BtTimeout: " + String.valueOf(bluetoothHandler.getScanCounterTimeout()) + "\n",
                "WifiTimeout: " + String.valueOf(wifiHandler.getScanCounterTimeout()) + "\n"
        };
        return values;
    }

}



