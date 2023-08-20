package com.dyson.tech.touchgesture.utils;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeHelper {

    public static final String TIME_FORMAT = "HH:mm";
    public static final String DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm";
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    private static TimeHelper helper;

    public static synchronized TimeHelper init() {
        if (helper == null) {
            helper = new TimeHelper();
        }
        return helper;
    }

    public String getTime(long time, String timeFormat) {
        Date date = new Date(time);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timeFormat);
        return simpleDateFormat.format(date);
    }

    public long getTimeMillis(String dateTime, String format) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        try {
            return dateFormat.parse(dateTime).getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}

