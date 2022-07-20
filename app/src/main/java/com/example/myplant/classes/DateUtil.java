package com.example.myplant.classes;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtil {

    public static long GetDateTimeLong(String data) {
        try {
            DateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            long dateLong = parser.parse(data).getTime();

            return dateLong / 1000;
        } catch (Exception e) {

        }
        return 0;
    }

    public static String covertDateToAgo(Date date) {

        String convTime = null;

        String prefix = "";
        String suffix = "Ago";

        try {
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//            Date pasTime = dateFormat.parse(dataDate);

            Date nowTime = new Date();

            long dateDiff = nowTime.getTime() - date.getTime();

            long second = TimeUnit.MILLISECONDS.toSeconds(dateDiff);
            long minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff);
            long hour = TimeUnit.MILLISECONDS.toHours(dateDiff);
            long day = TimeUnit.MILLISECONDS.toDays(dateDiff);

            if (second < 60) {
                convTime = second + " Seconds " + suffix;
            } else if (minute < 60) {
                convTime = minute + " Minutes " + suffix;
            } else if (hour < 24) {
                convTime = hour + " Hours " + suffix;
            } else if (day >= 7) {
                if (day > 360) {
                    convTime = (day / 360) + " Years " + suffix;
                } else if (day > 30) {
                    convTime = (day / 30) + " Months " + suffix;
                } else {
                    convTime = (day / 7) + " Week " + suffix;
                }
            } else if (day < 7) {
                convTime = day + " Days " + suffix;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ConvTimeE", e.getMessage());
        }

        return convTime;
    }

    public static long GetDateTimeLongCalender(int year, int month, int day, int hour, int min) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, hour, min, 0);

//            calendar.add(Calendar.DAY_OF_MONTH, addDays);

//            DateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            long dateLong = calendar.getTimeInMillis();
//            long dateLong = parser.parse(data).getTime();

            return dateLong / 1000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static Date GetDateWithAddNextDays(int addNextDays) {
        Calendar c = Calendar.getInstance();
//        int year = c.get(Calendar.YEAR);
//        int month = c.get(Calendar.MONTH);
        if (addNextDays > 0) {
            c.add(Calendar.DAY_OF_MONTH, addNextDays);
        }
        c.set(Calendar.HOUR, 9);
        c.set(Calendar.MINUTE, 0);

//        int day = c.get(Calendar.DAY_OF_MONTH);
//        int day = c.get(Calendar.DAY_OF_MONTH);

        return c.getTime();
    }

    public static long GetDateOnlyLong(String data) {
        try {
            DateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
            long dateLong = parser.parse(data).getTime();

            return dateLong / 1000;
        } catch (Exception e) {

        }
        return 0;
    }
}
