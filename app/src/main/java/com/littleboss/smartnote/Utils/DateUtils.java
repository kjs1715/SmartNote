package com.littleboss.smartnote.Utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static final String format = "yyyyMMdd_HHmmss";
    private static SimpleDateFormat formatter;
    public static final String display_format = "yyyy-MM-dd HH:mm:ss";
    private static SimpleDateFormat display_formatter;

    final private static SimpleDateFormat getFormatter() {
        if (formatter==null) {
            formatter = new SimpleDateFormat(format);
        }
        return formatter;
    }

    final private static SimpleDateFormat getDisplay_formatter() {
        if (display_formatter==null) {
            display_formatter = new SimpleDateFormat(display_format);
        }
        return display_formatter;
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
