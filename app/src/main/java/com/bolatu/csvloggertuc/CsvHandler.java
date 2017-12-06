package com.bolatu.csvloggertuc;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by bolatu on 5/13/16.
 */
public class CsvHandler {

    public static final int MAGNETIC_FIELD = 1;
    public static final int WIFI = 2;
    public static final int BLUETOOTH = 3;
    public static final int CELL = 4;

    private String mfHeaderCsv =
            "Coor X," + "Coor Y," + "Floor," + "Ref Label," + "CURRENT TIME NANOSECONDS," +"CURRENT TIME STRING," + "SYSTIME NANOSECONDS," + "SYSTIME STRING," +
                    "LOCAL MF X,LOCAL MF Y,LOCAL MF Z,"+
                    "NATIVE ORIENTATION X,NATIVE ORIENTATION Y,NATIVE ORIENTATION Z," +
                    "ACCELERATION X,ACCELERATION Y,ACCELERATION Z," +
                    "GYRO ORIENTATION X,GYRO ORIENTATION Y,GYRO ORIENTATION Z," +
                    "ROT X,ROT Y,ROT Z\n";

    private File sdCard = Environment.getExternalStorageDirectory();
    private File dir = new File(sdCard.getAbsolutePath() + "/CsvLoggerTUC");
    private File fileCsv;


    public CsvHandler(int type){
        switch (type) {
            case MAGNETIC_FIELD:
                fileCsv = new File(dir, "MF.csv");
                try {
                    writeHeader(mfHeaderCsv);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            case WIFI:
                fileCsv = new File(dir, "WIFI.csv");
                return;
            case BLUETOOTH:
                fileCsv = new File(dir, "BLUETOOTH.csv");
                return;
            case CELL:
                fileCsv = new File(dir, "CELL.csv");
                return;
        }
    }

    public void writeHeader(String content) throws IOException {
        if (!fileCsv.exists()) {
            dir.mkdir();
            fileCsv.createNewFile();
            FileWriter fileWriter = new FileWriter(sdCard.getAbsolutePath() + "/CsvLoggerTUC/" + fileCsv.getName());
            BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
            bufferWriter.write(content);
            bufferWriter.close();
        }
    }


    public void write(String content) throws IOException {
        FileWriter fileWriter = new FileWriter(sdCard.getAbsolutePath() + "/CsvLoggerTUC/" + fileCsv.getName(), true);
        BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
        bufferWriter.write(content);
        bufferWriter.close();
//        Log.d("CsvHandler","Data is written into CSV");
    }
}
