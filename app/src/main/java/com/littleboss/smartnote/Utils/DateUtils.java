package com.littleboss.smartnote.Utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static final String FORMAT = "yyyyMMdd_HHmmss";
    public static final String DISPLAYFORMAT = "yyyy-MM-dd HH:mm:ss";

    final private static SimpleDateFormat getFormatter() {
        return new SimpleDateFormat(FORMAT);
    }

    final private static SimpleDateFormat getDisplay_formatter() {
        return new SimpleDateFormat(DISPLAYFORMAT);
    }

    public static String display(Date date) {
        return getDisplay_formatter().format(date);
    }

    public static String Date2String(Date date) {
        return getFormatter().format(date);
    }

    public static Date String2Date(String date) {
        try {
            return getFormatter().parse(date);
        } catch (ParseException e) {
            Log.e("", e.toString());
            return null;
        }
    }
}
