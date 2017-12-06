package com.bolatu.csvloggertuc;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.bolatu.csvloggertuc.GyroOrientation.GyroscopeOrientation;
import com.bolatu.csvloggertuc.GyroOrientation.Orientation;

import java.io.IOException;

/**
 * Created by bolatu on 5/11/16.
 */
public class MagneticFieldHandler implements SensorEventListener {

    private TimeStampHandler timeStampHandler = new TimeStampHandler();
    private CsvHandler csvHandler;

    // Control variables
    private boolean isScanFinished = false;
    private boolean isMfReceived = false;
    private boolean isOrientationReceived = false;
    private boolean isAcceReceived = false;
    private boolean isRotationVectorReceived = false;
    private boolean isScanSuccessfullyFinished = false;



    // Since we are running this WiFi scan process in the UI Thread,
    // we need pass the context from the activity
    private Context mContext;

    // Mf variables
    private SensorManager sensorManager;
    private Sensor magneticField;
    private Sensor rotationSensor;
    private Sensor orientationSensor;
    private Sensor acceSensor;
    private Sensor gyroSensor;
    private double localMfX;
    private double localMfY;
    private double localMfZ;
    private float magnetic[] = new float[3];
    private String mfDataCsv;

    private double orientationX = 0;
    private double orientationY = 0;
    private double orientationZ = 0;

    private double acceX = 0;
    private double acceY = 0;
    private double acceZ = 0;

    private double rotX = 0;
    private double rotY = 0;
    private double rotZ = 0;

    private long timestampOfMfDetection;
    private String convertedMfTimestamp;
    private long timestampOfOrientationDetection;
    private String convertedOrientationTimestamp;
    private long timestampOfAcceDetection;
    private String convertedAcceTimestamp;
    private long timestampOfRotationVectorDetection;


    private int scanCounter = 0;

    private Orientation orientationGyro;
    private double gyroOrientationX = 0;
    private double gyroOrientationY = 0;
    private double gyroOrientationZ = 0;


    // Constructor
    public MagneticFieldHandler(Context context) {
        this.mContext = context;
    }

    // startMfScan registers the sensormanager and starts the scan
    public void startMfScan() {
        // Initializing the MF
        isScanFinished = false;
        isScanSuccessfullyFinished = false;
        isMfReceived = false;
        isOrientationReceived = false;
        isAcceReceived = false;
        isRotationVectorReceived = false;
        scanCounter = 0;

        csvHandler = new CsvHandler(CsvHandler.MAGNETIC_FIELD);

        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
//        if (Sensor.TYPE_MAGNETIC_FIELD != null) {
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        acceSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_FASTEST);
        }
        else {
            isMfReceived = true;
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        else {
            isRotationVectorReceived = true;
        }

        if (orientationSensor != null) {
            sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        else {
            isOrientationReceived = true;
        }

        if (acceSensor != null) {
            sensorManager.registerListener(this, acceSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        else {
            isAcceReceived = true;
        }

        if (gyroSensor != null) {
            orientationGyro = new GyroscopeOrientation(mContext);
            orientationGyro.onResume();
        }



//        scanMagneticField();
    }

    // stopMfScan unregisters the sensormanager
    public void stopMfScan() {

        if (!isScanFinished) {
            isScanFinished = true;

            isMfReceived = false;
            isOrientationReceived = false;
            isAcceReceived = false;

            scanCounter = 0;

            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;
        // If the received results are MF variables, then we take them
        if (magneticField != null) {
            if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                timestampOfMfDetection = sensorEvent.timestamp;
                isMfReceived = true;

                magnetic[0] = sensorEvent.values[0];
                magnetic[1] = sensorEvent.values[1];
                magnetic[2] = sensorEvent.values[2];

                localMfX = magnetic[0];
                localMfY = magnetic[1];
                localMfZ = magnetic[2];
            }
        }
        else{
            isMfReceived = true;
        }

        if (orientationSensor != null) {
            if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
                timestampOfOrientationDetection = sensorEvent.timestamp;
                isOrientationReceived = true;

                orientationX = sensorEvent.values[1];
                orientationY = sensorEvent.values[2];
                orientationZ = sensorEvent.values[0];
            }
        }
        else{
            isOrientationReceived = true;
        }

        if (acceSensor != null) {
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                timestampOfAcceDetection = sensorEvent.timestamp;
                isAcceReceived = true;

                acceX = sensorEvent.values[0];
                acceY = sensorEvent.values[1];
                acceZ = sensorEvent.values[2];
            }
        }
        else {
            isAcceReceived = true;
        }

        if (rotationSensor != null) {
            if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                timestampOfRotationVectorDetection = sensorEvent.timestamp;
                isRotationVectorReceived = true;

                float[] orientation = new float[3];
                float[] rMat = new float[9];

                // calculate th rotation matrix
                SensorManager.getRotationMatrixFromVector(rMat, sensorEvent.values);
                SensorManager.getOrientation(rMat, orientation);

                rotX = Math.toDegrees(orientation[1]);
                rotY = Math.toDegrees(orientation[2]);
                rotZ = Math.toDegrees(orientation[0]);
            }
        }
        else {
            isRotationVectorReceived = true;
        }

        if (isMfReceived && isOrientationReceived && isAcceReceived && isRotationVectorReceived && !isScanFinished) {

            Log.d("onSensorChanged", "1");


            isMfReceived = false;
            isOrientationReceived = false;
            isAcceReceived = false;

//            convertedMfTimestamp = timeStampHandler.convertTimestamp(timestampOfMfDetection);
//            // Ori and Acce are not used to log at the moment
//            convertedOrientationTimestamp = timeStampHandler.convertTimestamp(timestampOfOrientationDetection);
//            convertedAcceTimestamp = timeStampHandler.convertTimestamp(timestampOfAcceDetection);

            long maxTimestamp = Math.max(Math.max(Math.max(timestampOfMfDetection, timestampOfOrientationDetection), timestampOfAcceDetection), timestampOfRotationVectorDetection);
            String convertedMaxTimestamp = timeStampHandler.convertTimestamp(maxTimestamp);


            if (gyroSensor != null) {
                float[] vOrientation = orientationGyro.getOrientation();
                gyroOrientationX = vOrientation[1];
                gyroOrientationY = vOrientation[2];
                gyroOrientationZ = vOrientation[0];
            }

            mfDataCsv =
                    MainActivity.coorX + "," +
                            MainActivity.coorY + "," +
                            MainActivity.floor + "," +
                            MainActivity.refLabel + "," +
                            timeStampHandler.updateTimeNanoseconds() + "," +
                            timeStampHandler.updateTimeString() + "," +
                            maxTimestamp + "," +
                            convertedMaxTimestamp + "," +
                            String.valueOf(localMfX) + "," +
                            String.valueOf(localMfY) + "," +
                            String.valueOf(localMfZ) + "," +
//                                        String.valueOf(Math.toDegrees(orientationX)) + "," +
//                                        String.valueOf(Math.toDegrees(orientationY)) + "," +
//                                        String.valueOf(Math.toDegrees(orientationZ)) + "," +
                            String.valueOf(orientationX) + "," +
                            String.valueOf(orientationY) + "," +
                            String.valueOf(orientationZ) + "," +
                            String.valueOf(acceX) + "," +
                            String.valueOf(acceY) + "," +
                            String.valueOf(acceZ) + "," +
                            String.valueOf(Math.toDegrees(gyroOrientationX)) + "," +
                            String.valueOf(Math.toDegrees(gyroOrientationY)) + "," +
                            String.valueOf(Math.toDegrees(gyroOrientationZ)) + "," +
                            String.valueOf(rotX) + "," +
                            String.valueOf(rotY) + "," +
                            String.valueOf(rotZ) + "\n";

            Log.d("rot",
                    String.valueOf(rotX) + "," +
                    String.valueOf(rotY) + "," +
                    String.valueOf(rotZ));

            try {
                Log.d("onSensorChanged", "is ready");
                csvHandler.write(mfDataCsv);
            } catch (IOException e) {
                e.printStackTrace();
            }

            scanCounter++;

//                        Log.d("MagneticFieldHandler", mfJsonString);
//                        Log.d("MagneticFieldHandler", mfDataCsv);
        }

        if(scanCounter >= MainActivity.scanNo) {
            isScanSuccessfullyFinished = true;
            stopMfScan();
            Log.d("onSensorChanged", "3 " + scanCounter + " " + MainActivity.scanNo);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // Setters and Getters
    public boolean isScanFinished() {
        return isScanFinished;
    }

    public boolean isMfReceived() {
        return isMfReceived;
    }

    public void setScanFinished(boolean scanFinished) {
        isScanFinished = scanFinished;
    }

    public double getLocalMfX() {
        return localMfX;
    }

    public double getLocalMfY() {
        return localMfY;
    }

    public double getLocalMfZ() {
        return localMfZ;
    }

    public int getScanCounter() {
        return scanCounter;
    }


    public boolean isScanSuccessfullyFinished() {
        return isScanSuccessfullyFinished;
    }

    public void setScanSuccessfullyFinished(boolean ScanSuccessfullyFinished) {
        isScanSuccessfullyFinished = ScanSuccessfullyFinished;
    }
}
