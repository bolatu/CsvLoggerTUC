package com.bolatu.csvloggertuc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by bolatu on 5/13/16.
 */
public class TimeStampHandler {

    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public String updateTimeString() {
        Date currentDate = new Date();
//        String date = dateFormat.format(currentDate);
//        String time = timeFormat.format(currentDate);
        String currentTime = format.format(currentDate);
        return currentTime;
    }

    public long updateTimeNanoseconds() {
        long timestamp = System.nanoTime();
        return timestamp;
    }


    public String convertTimestamp(long time) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();

        /* date formatter in local timezone */
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        sdf.setTimeZone(tz);

        /* print your timestamp and double check it's the date you expect */
        long timestamp = time / 1000000;
        String localTime = sdf.format(new Date(timestamp)); // I assume your timestamp is in seconds and you're converting to milliseconds?
        return localTime;
    }
}
